
package p2pfilesharer.transfer.impl;

import com.google.common.collect.RangeSet;
import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.tomp2p.dht.PeerDHT;
import p2pfilesharer.transfer.DownloadStore;
import p2pfilesharer.transfer.DownloadStore.StoreKey;
import p2pfilesharer.transfer.RemoteFile;
import p2pfilesharer.transfer.RunnableTransfer;
import p2pfilesharer.transfer.Transfer;
import p2pfilesharer.transfer.TransferFactory;
import p2pfilesharer.transfer.impl.StreamManager;

/**
 *
 * @author Nick
 */
public class DefaultTransferFactory implements TransferFactory {

    final int MAX_SIMUL_DOWNLOADS = 3;
    final int MAX_SIMUL_RETRIES = 3;

    final StreamManager streamManager;
    final EventBus eventBus;
    final ExecutorService firstTryExecutor = Executors.newFixedThreadPool(MAX_SIMUL_DOWNLOADS);
    final ExecutorService retryExecutor = Executors.newFixedThreadPool(MAX_SIMUL_RETRIES);

    public DefaultTransferFactory(StreamManager streamManager, EventBus eventBus) {
        this.streamManager = streamManager;
        this.eventBus = eventBus;
    }

    @Override
    public Transfer download(PeerDHT thisPeer, RemoteFile file, DownloadStore fileDao, RangeSet<Long> range) throws IOException {
        StoreKey storeKey = fileDao.create(file);
        RunnableTransfer transfer = new RunnableTransfer(thisPeer, file, storeKey, fileDao, streamManager, eventBus);
        Callable<Boolean> call = () -> {
            transfer.call();
            return fileDao.finish(storeKey);
        };
        firstTryExecutor.submit(call);
        return transfer;
    }

}
