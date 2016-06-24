package p2pfilesharer.publish;


import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import net.tomp2p.dht.PeerDHT;


/**
 *
 * @author Nick
 */
public interface Where<T> {
    
    Where<T> and(Where<T> that);
    
    Where<T> or(Where<T> that);
    
    ListenableFuture<Set<T>> lookup(PeerDHT thisPeer, int limit);
    
}
