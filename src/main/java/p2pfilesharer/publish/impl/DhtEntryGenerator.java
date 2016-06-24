package p2pfilesharer.publish.impl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.tomp2p.peers.Number160;
import static p2pfilesharer.common.Util.*;
/**
 *
 * @author Nick
 */
public class DhtEntryGenerator<K> {

    public interface Locater {

        Set<Number160> locate(Field attr, Object val);
    }

    private final static class EqualsLocater implements Locater {

        @Override
        public Set<Number160> locate(Field attr, Object val) {
            if (val instanceof Number160) {
                return Collections.singleton((Number160) val);
            } else if (val instanceof String) {
                return Collections.singleton(Number160.createHash((String) val));
            } else if (val instanceof Long) {
                return Collections.singleton(Number160.createHash((Long) val));
            } else if (val instanceof Integer) {
                return Collections.singleton(Number160.createHash((Integer) val));
            } else {
                throw new IllegalArgumentException("val must be a String, int, or long");
            }
        }
    }

    private final static class EqualsIgnoreCaseLocater implements Locater {

        @Override
        public Set<Number160> locate(Field attr, Object val) {
            if (!(val instanceof String)) {
                throw new IllegalArgumentException("val must be a String");
            }
            String s = (String) val;
            EqualsLocater el = new EqualsLocater();
            return el.locate(attr, s.toLowerCase());
        }
    }

    private final static class KeywordLocater implements Locater {

        @Override
        public Set<Number160> locate(Field attr, Object val) {
            if (!(val instanceof Collection)) {
                throw new IllegalArgumentException("val must be a Collection of Strings");
            }

            Collection<String> ws = (Collection<String>) val;
            Set<Number160> locs = new HashSet<>();
            for (String w1 : ws) {
                Number160 hash1 = Number160.createHash(w1.toLowerCase());
                locs.add(hash1);
                for (String w2 : ws) {
                    if (!w1.equalsIgnoreCase(w2)) {
                        Number160 hash2 = Number160.createHash(w2.toLowerCase());
                        Number160 hashXor = hash1.xor(hash2);
                        locs.add(hashXor);
                    }
                }
            }
            return locs;
        }
    }

    public enum LocaterType {
        EQUALS(new EqualsLocater()),
        EQUALS_IGNORE_CASE(new EqualsIgnoreCaseLocater()),
        CONTAINS_KEYWORDS(new KeywordLocater());

        Locater loc;

        LocaterType(Locater loc) {
            this.loc = loc;
        }
    }

    private Class clazz;
    private Field idField;
   // private Map<String, Function<K, Object>> attributes;
   // private Map<String, Locater> locaters;
    private Map<Field, Locater> clazzLocationFields = new ConcurrentHashMap<>();
    private Number160 clazzDomain;

    public DhtEntryGenerator(Class clazz, Field idField) {
        this.clazz = clazz;
        this.idField = idField;
        this.clazzDomain = Number160.createHash(clazz.getName());
    }
    
    public void addIndex(Field field, LocaterType locType) {
        clazzLocationFields.put(field, locType.loc);
    }

    public Class getClazz() {
        return clazz;
    }

    public Number160 getClassDomainKey() {
        return clazzDomain;
    }

    public Set<Field> getIndexedAttributes() {
        return clazzLocationFields.keySet();
    }

    public Set<DhtEntry> generateDhtEntries(Object obj) {
        Number160 domKey = clazzDomain;
        Number160 contKey = (Number160) getValue(idField, obj);
        return clazzLocationFields.entrySet().stream()
                .map(e -> e.getValue().locate(e.getKey(), obj))
                .map(s -> s.stream()
                        .map(t -> new DhtEntry(obj, contKey, domKey, t))
                        .collect(Collectors.toSet()))
                .reduce(new HashSet<>(), (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });
    }

    public Set<DhtLocation> generateDhtLocations(Field attr, Object val, LocaterType locType) {
        return locType.loc.locate(attr, val)
                .stream()
                .map(h -> new DhtLocation(clazzDomain, h))
                .collect(Collectors.toSet());
    }
}
