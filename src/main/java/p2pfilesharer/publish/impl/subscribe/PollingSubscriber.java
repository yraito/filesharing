package p2pfilesharer.publish.impl.subscribe;


import com.google.common.eventbus.EventBus;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureImpl;
import net.tomp2p.futures.Cancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import p2pfilesharer.publish.SubscriptionEvent;
import p2pfilesharer.publish.SubscriptionEvent.Type;
import p2pfilesharer.publish.Searcher;
import p2pfilesharer.publish.Subscriber;
import p2pfilesharer.publish.impl.DhtEntryGenerator;



/**
 *
 * @author Nick
 */
public class PollingSubscriber<K> implements Subscriber<K> {
    
    private final static Logger logger = LoggerFactory.getLogger(PollingSubscriber.class);
    
    private final Class<K> clazz;
    private final Searcher<K> searcher;
    private final DhtEntryGenerator entryGen;
    private final ScheduledExecutorService executor;

    public PollingSubscriber(Class<K> clazz, Searcher<K> searcher, DhtEntryGenerator entryGen) {
        this.clazz = clazz;
        this.searcher = searcher;
        this.entryGen = entryGen;
        this.executor = Executors.newScheduledThreadPool(4);
    }
    
    @Override
    public boolean canSubscribeByField(Field f) {
        return entryGen.getIndexedAttributes().contains(f);
    }

    @Override
    public Cancel subscribeByField(Field f, Object o, PeerDHT p, EventBus e) {
        int POLL_MAX_RESULTS = 1000;
        int POLL_TIMEOUT = 30;
        int POLL_INTERVAL = 30;
        
        Runnable r = () -> {
            try {
                Set<K> res = searcher
                        .whereEquals(f, o)
                        .lookup(p, POLL_MAX_RESULTS)
                        .get(POLL_TIMEOUT, TimeUnit.SECONDS);
                e.post(SubscriptionEvent.normal(clazz, f, Type.RENEW, res));
            } catch (InterruptedException ex) {
                logger.error("Poll task interrupted");
            } catch (TimeoutException ex) {
                logger.error("Poll task timed out: {}", ex.getMessage());
                e.post(SubscriptionEvent.error(clazz, f, ex));
            } catch (ExecutionException ex) {
                Throwable t = ex.getCause();
                logger.error("Poll task exception: {}. {}", t.getClass(), t.getMessage());
                e.post(SubscriptionEvent.error(clazz, f, t));
            } 
        };
     
        ScheduledFuture<?> sf = executor.scheduleWithFixedDelay(
               r, 
               0, 
               POLL_INTERVAL, 
               TimeUnit.SECONDS
        );
        
        return () -> {
            sf.cancel(true);
        };
        
       
    }

    @Override
    public BaseFuture shutdown() {
        return new BaseFutureImpl() { 
            {
                executor.shutdownNow();
            }
        };
    }
}
