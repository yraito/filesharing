
package p2pfilesharer.p2px;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.FutureForkJoin;
import net.tomp2p.p2p.Shutdown;

/**
 *
 * @author Nick
 */
public class AllShutdown implements Shutdown {

    final List<Shutdown> shutdowns = Collections.synchronizedList(new LinkedList<>());
    
    public AllShutdown(Collection<Shutdown> shutdowns) {
        this.shutdowns.addAll(shutdowns);
    }
    
    public void addShutdown(Shutdown shutdown) {
        this.shutdowns.add(shutdown);
    }
    
    @Override
    public BaseFuture shutdown() {
        BaseFuture[] bfs = shutdowns.stream()
                .map(Shutdown::shutdown)
                .collect(Collectors.toList())
                .toArray(new BaseFuture[shutdowns.size()]);
        return new FutureForkJoin(new AtomicReferenceArray(bfs));
    }
    
}
