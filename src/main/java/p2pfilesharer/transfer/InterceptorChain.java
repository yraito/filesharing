package p2pfilesharer.transfer;


import java.util.ArrayList;
import java.util.List;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.PeerAddress;

/**
 *
 * @author Nick
 */
public class InterceptorChain {
    
    final List<Interceptor> interceptors;
    final int index;
    
    public InterceptorChain(List<Interceptor> interceptors) {
        this.interceptors = new ArrayList<>();
        this.interceptors.addAll(interceptors);
        this.index = 0;
    }
    
    private InterceptorChain(List<Interceptor> interceptors, int index) {
        this.interceptors = interceptors;
        this.index = index;
    }
    
    public Object nextInterceptor(PeerDHT thisPeer, PeerAddress thatPeer, Object obj) { 
        if (index >= interceptors.size()) {
            return "No interceptor...";
        }
        InterceptorChain ic1 = new InterceptorChain(interceptors, index + 1); 
        return interceptors.get(index).reply(obj, thisPeer, thatPeer, ic1);
    }
}
