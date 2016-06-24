package p2pfilesharer.publish;


import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;


/**
 *
 * @author Nick
 */
public class SubscriptionEvent<K> {
    
    public static <K> SubscriptionEvent error(Class<K> c, Field f, Throwable t) {
        return new SubscriptionEvent(c, f, Type.ERROR, null, t);
    }
    
    public static <K> SubscriptionEvent normal(Class<K> c, Field f, Type t, Set<K> s) {
        return new SubscriptionEvent(c, f, t, s, null);
    }
    
    public enum Type {
        RENEW, INSERT, REMOVE, MODIFY, ERROR
    }
    
    final Class<K> publishedItemClazz;
    final Field publishedByField;
    final Type type;
    final Set<K> elements;
    final Throwable throwable;
    
    private SubscriptionEvent(Class<K> publishedItemClazz, Field publishedByField, Type type, Set<K> elements, Throwable throwable) {
        this.publishedItemClazz = publishedItemClazz;
        this.publishedByField = publishedByField;
        this.type = type;
        this.elements = elements;
        this.throwable = throwable;
    }

    public Class<K> getPublishedItemClazz() {
        return publishedItemClazz;
    }

    public Field getPublishedByField() {
        return publishedByField;
    }

    public Type getType() {
        return type;
    }

    public Set<K> getElements() {
        return Collections.unmodifiableSet(elements);
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
