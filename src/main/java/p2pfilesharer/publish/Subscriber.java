package p2pfilesharer.publish;


import com.google.common.eventbus.EventBus;
import java.lang.reflect.Field;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.Cancel;
import net.tomp2p.p2p.Shutdown;


/**
 *
 * @author Nick
 */
public interface Subscriber<K> extends Shutdown {
    
    
    boolean canSubscribeByField(Field f);
    
    Cancel subscribeByField(Field f, Object o, PeerDHT p, EventBus e);
}
