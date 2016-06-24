package p2pfilesharer.publish;


import java.lang.reflect.Field;
import java.util.Set;



/**
 *
 * @author Nick
 */
public interface Searcher<K> {
    
    /**
     * Query for any and all published K's on the network.
     * 
     * @return 
     */
    Where<K> whereAll();
    
    /**
     * Query for published K's for which the value of Field f equals the given object.
     * 
     * @param f 
     * @param o
     * @return 
     */
    Where<K> whereEquals(Field f, Object o);
    
    /**
     * Query for published K's for which the value of Field f (assumed to be a String) equals
     * the given object, ignoring case.
     * 
     * @param f a Field whose type is String
     * @param s the String to search for
     * @return 
     */
    Where<K> whereLowerEquals(Field f, String s);
    
    /**
     * Query for published K's for which the value of Field f (assumed to be a Collection of Strings)
     * contains all of they keywords in the given set, ignoring case.
     * 
     * @param f a Field whose type is a Collection of Strings
     * @param ks the keywords to search for
     * @return 
     */
    Where<K> whereLowerContainsKeywords(Field f, Set<String> ks);
}
