package p2pfilesharer.publish.impl.search;

import p2pfilesharer.publish.impl.DhtLocation;
import p2pfilesharer.publish.Where;
import p2pfilesharer.publish.Searcher;
import static com.google.common.base.Preconditions.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.tomp2p.peers.Number160;
import p2pfilesharer.publish.impl.DhtEntryGenerator;
import static p2pfilesharer.publish.impl.DhtEntryGenerator.LocaterType;
import p2pfilesharer.common.Util;

/**
 *
 * @author Nick
 */
public class DefaultSearcher<K> implements Searcher<K> {

    final DhtEntryGenerator entryGen;

    public DefaultSearcher(DhtEntryGenerator entryGen) {
        this.entryGen = entryGen;
    }

    @Override
    public Where<K> whereAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Where<K> whereEquals(Field f, Object o) {
        checkNotNull(o);
        Class<?> clazz = entryGen.getClazz();
        Set<DhtLocation> locs = entryGen.generateDhtLocations(f, o, LocaterType.EQUALS);
        Predicate pred = k -> Objects.equals(o, Util.getValue(f, k));
        return new BaseWhere(clazz, locs, pred);
    }

    public Where<K> whereLowerEquals(Field f, String s) {
        checkNotNull(s);
        checkArgument(
                String.class.isAssignableFrom(f.getType()),
                "Field " + f.getDeclaringClass() + "::" + f.getName() + " must be a String type"
        );
        Class<?> clazz = entryGen.getClazz();
        Set<DhtLocation> locs = entryGen.generateDhtLocations(f, s, LocaterType.EQUALS_IGNORE_CASE);
        Predicate pred = k -> s.equalsIgnoreCase((String) Util.getValue(f, k));
        return new BaseWhere(clazz, locs, pred);

    }

    @Override
    public Where<K> whereLowerContainsKeywords(Field f, Set<String> ks) {
        checkNotNull(ks);
        checkArgument(
                Collection.class.isAssignableFrom(f.getType()),
                "Field " + f.getDeclaringClass() + "::" + f.getName() + " must be a Collection type"
        );

        if (ks.isEmpty()) {
            return whereAll();
        }
        if (ks.size() == 1) {
            return whereLowerEquals(f, ks.iterator().next());
        }

        Class clazz = entryGen.getClazz();
        Set<Number160> locs = entryGen.generateDhtLocations(f, ks, LocaterType.CONTAINS_KEYWORDS);
        Predicate<K> pred = k -> ( (Collection<K>) Util.getValue(f, k) ).containsAll(ks);
        return new BaseWhere(clazz, locs, pred);
    }

}
