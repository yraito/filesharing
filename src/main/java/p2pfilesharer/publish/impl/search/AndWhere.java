package p2pfilesharer.publish.impl.search;


import p2pfilesharer.publish.Where;
import com.google.common.base.Function;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.tomp2p.dht.PeerDHT;


/**
 *
 * @author Nick
 */
public class AndWhere<K> extends AbstractWhere<K> {

    private final Where<K> first;
    private final Where<K> second;

    public AndWhere(Where<K> first, Where<K> second) {
        checkNotNull(first);
        checkNotNull(second);
        this.first = first;
        this.second = second;
    }

    @Override
    public ListenableFuture<Set<K>> lookup(PeerDHT thisPeer, int limit) {
        
        ListenableFuture<List<Set<K>>> allFut = Futures.allAsList(
                first.lookup(thisPeer, limit),
                second.lookup(thisPeer, limit)
        );
        
        Function<List<Set<K>>, Set<K>> func = sl -> {
            return sl.stream().reduce(new HashSet<>(), (s, u) -> {
                return Sets.intersection(s, u);
            });
        };
        
        return Futures.transform(allFut, func);
    }
    
    @Override
    public String toString() {
        return "(" + first + " AND " + second + ")";
    }
}
