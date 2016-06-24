package p2pfilesharer.publish.impl.publish;

import p2pfilesharer.publish.impl.DhtEntry;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.PutBuilder;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.p2p.AutomaticFuture;
import net.tomp2p.p2p.JobScheduler;
import net.tomp2p.p2p.Shutdown;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import p2pfilesharer.p2px.AllShutdown;
import p2pfilesharer.publish.Publisher;
import p2pfilesharer.publish.impl.DhtEntryGenerator;

/**
 *
 * @author Nick
 */
public class DefaultPublisher<K> implements Publisher<K> {

    private final static Logger logger = LoggerFactory.getLogger(DefaultPublisher.class);

    private final DhtEntryGenerator dhtEntryGenerator;
    private final Set<Shutdown> shutdowns = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DefaultPublisher(DhtEntryGenerator entryGen) {
        this.dhtEntryGenerator = entryGen;
    }

    @Override
    public Shutdown publish(PeerDHT p, K k, AutomaticFuture a) throws IOException {
        Set<DhtEntry> entries = dhtEntryGenerator.generateDhtEntries(k);
        logger.info("Publish: Putting into DHT {} entries for {}: {}", entries.size(), dhtEntryGenerator.getClazz(), k);
        List<Shutdown> shtdwns = entries.stream()
                .map(e -> publish(p, e, a))
                .collect(Collectors.toList());
        Shutdown shtdwn = new AllShutdown(shtdwns);
        this.executor.submit(() -> shutdowns.add(shtdwn));
        return () -> {
            this.executor.submit(() -> shutdowns.remove(shtdwn));
            return shtdwn.shutdown();
        };

    }

    private Shutdown publish(PeerDHT p, DhtEntry e, AutomaticFuture a) {
        int PUT_INTERVAL = 60;
        int PUT_REPETITIONS = -1;
        AutomaticFuture a2 = a != null ? a : bf -> {
            logger.debug("Put {}", e);
        };
        logger.debug("Putting entry for {}: {}", dhtEntryGenerator.getClazz(), e);
        try {
            Data data = new Data(e.obect);
            PutBuilder put = p.put(e.locationKey).data(e.domainKey, e.idKey, data);
            JobScheduler job = new JobScheduler(p.peer());
            return job.start(put, PUT_INTERVAL, PUT_REPETITIONS, a2);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public BaseFuture shutdown() {
        logger.info("Shutting down publisher");
        Callable<BaseFuture> c = () -> {
            return new AllShutdown(shutdowns).shutdown();
        };
        try {
            return executor.submit(c).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

}
