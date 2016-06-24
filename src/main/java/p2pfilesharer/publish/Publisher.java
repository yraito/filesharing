package p2pfilesharer.publish;


import java.io.IOException;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.AutomaticFuture;
import net.tomp2p.p2p.Shutdown;


/**
 *
 * @author Nick
 */
public interface Publisher<K> extends Shutdown {
    
    Shutdown publish(PeerDHT p, K k, AutomaticFuture a) throws IOException ;
   
}
