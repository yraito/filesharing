package p2pfilesharer.publish.impl.search;

import p2pfilesharer.publish.impl.DhtLocation;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import net.tomp2p.dht.FutureSend;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.Cancel;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import p2pfilesharer.p2px.DataX;

/**
 *
 * @author Nick
 */
public class BaseWhere<K> extends AbstractWhere<K> {

    private final static Logger logger = LoggerFactory.getLogger(BaseWhere.class);

    private final Set<DhtLocation> searchLocations;
    private final Predicate<K> matchesPredicate;
    private final String string;

    public BaseWhere(Class<?> clazz, Set<DhtLocation> searchLocations, Predicate<K> matchesPredicate) {
        this(clazz, searchLocations, matchesPredicate, null);
    }

    public BaseWhere(Class<?> clazz, Set<DhtLocation> searchLocations, Predicate<K> matchesPredicate, String string) {
        this.searchLocations = searchLocations;
        this.matchesPredicate = matchesPredicate.and(k
                -> clazz.isAssignableFrom(k.getClass())
        );
        this.string = string;
    }

    @Override
    public ListenableFuture<Set<K>> lookup(PeerDHT thisPeer, int limit) {
        SettableFuture<Set<K>> futureLookup = SettableFuture.create();
        Set<Cancel> cancels = new HashSet<>();
        Lock lock = new ReentrantLock();
        BaseFutureListener rcfl = new ResultCompilingFutureListener(futureLookup, cancels, lock, limit);

        logger.debug("Lookup {}. Searching {} locations", this, this.searchLocations.size());
        lock.lock();
        try {
            for (DhtLocation dhtLoc : this.searchLocations) {
                QueryPacket queryPacket = new QueryPacket(
                        dhtLoc.locationKey,
                        dhtLoc.domainKey,
                        this.matchesPredicate,
                        limit
                );
                logger.debug("Sending query packet to {} ", dhtLoc);
                Data data = DataX.unchecked(queryPacket);
                FutureSend futureSend = thisPeer
                        .send(dhtLoc.locationKey)
                        .domainKey(dhtLoc.domainKey)
                        .object(data)
                        .start();
                cancels.add(futureSend);
                futureSend.addListener(rcfl);
            }
        } finally {
            lock.unlock();
        }

        return futureLookup;
    }

    @Override
    public String toString() {
        return "BaseWhere{" + string + '}';
    }
    
    

    private class ResultCompilingFutureListener implements BaseFutureListener<FutureSend> {

        final CountDownLatch latch = new CountDownLatch(searchLocations.size());
        final Set<K> allResults = new HashSet<>();

        final SettableFuture<Set<K>> futureLookup;
        final Set<Cancel> cancels;
        final Lock lock;
        final int limit;

        public ResultCompilingFutureListener(SettableFuture<Set<K>> futureLookup, Set<Cancel> cancels, Lock lock, int limit) {
            this.futureLookup = futureLookup;
            this.cancels = cancels;
            this.lock = lock;
            this.limit = limit;
        }

        @Override
        public void operationComplete(FutureSend f) throws Exception {
            Set<K> part = (Set<K>) f.object();
            allResults.addAll(part);
            latch.countDown();
            if (latch.getCount() == 0) {
                futureLookup.set(allResults);
            } else if (allResults.size() >= limit) {
                futureLookup.set(allResults);
                cancelAll();
            }
        }

        @Override
        public void exceptionCaught(Throwable thrwbl) throws Exception {
            futureLookup.setException(thrwbl);
            cancelAll();
        }

        void cancelAll() {
            lock.lock();
            try {
                cancels.forEach(Cancel::cancel);
            } finally {
                lock.unlock();
            }
        }
    }
}
