
package p2pfilesharer.common;

import java.util.concurrent.Callable;

/**
 *
 * @author Nick
 */
public interface PausableCallable<V> extends Callable<V> {
    
    Pausable getPause();
}
