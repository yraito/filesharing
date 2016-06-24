
package p2pfilesharer.publish.impl.search;

import p2pfilesharer.publish.Where;

/**
 *
 * @author Nick
 */
public abstract class AbstractWhere<K> implements Where<K> {

    @Override
    public Where<K> and(Where<K> that) {
        return new AndWhere(this, that);
    }

    @Override
    public Where<K> or(Where<K> that) {
        return new OrWhere(this, that);
    }
    
}
