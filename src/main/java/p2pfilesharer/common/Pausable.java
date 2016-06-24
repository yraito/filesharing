
package p2pfilesharer.common;


/**
 *
 * @author Nick
 */
public interface Pausable {
    
    void pause();
    
    void unpause();
    
    boolean isPaused();
}
