package p2pfilesharer.transfer;

import p2pfilesharer.transfer.impl.ResponsePacket;
import p2pfilesharer.transfer.impl.StreamManager;
import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FuturePeerConnection;
import net.tomp2p.peers.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import p2pfilesharer.transfer.DownloadStore.StoreKey;
import p2pfilesharer.transfer.impl.RequestPacket;
import p2pfilesharer.transfer.impl.RequestPacket.Method;
import p2pfilesharer.common.Pauser;
import p2pfilesharer.common.SeekableOutputStream;
import p2pfilesharer.common.TransferTracker;
import p2pfilesharer.common.Pausable;
import p2pfilesharer.common.PausableCallable;
import p2pfilesharer.common.PausableFutureTask;
import p2pfilesharer.common.RangeTracker;
import p2pfilesharer.transfer.impl.ResponsePacket.Status;

/**
 *
 * @author Nick
 */
public class RunnableTransfer implements Transfer, Callable<Void> {

    private final static Logger logger = LoggerFactory.getLogger(RunnableTransfer.class);

    //final AtomicReference<TransferStage> restartStage = new AtomicReference<>();
    //final AtomicReference<TransferStage> resumeStage = new AtomicReference<>();
    //final AtomicReference<TransferStage> prevStage = new AtomicReference<>();
    final AtomicReference<Object> currStage = new AtomicReference<>();
    //final AtomicReference<EventBus> eventManager = new AtomicReference<>();
    final AtomicReference<ExecutorService> execService = new AtomicReference<>();
    final AtomicBoolean started = new AtomicBoolean();
    final AtomicReference<PausableFutureTask<Void>> pauseFuture = new AtomicReference<>();

    
    final RemoteFile remoteFile;
    final PeerDHT thisPeer;
    final AtomicReference<String> state = new AtomicReference<>();
    final AtomicReference<RangeTracker> rangeTracker = new AtomicReference<>();
    final AtomicReference<TransferTracker> transferTracker = new AtomicReference<>();
    final AtomicReference<Semaphore> linesSemaphore = new AtomicReference<>();
    final AtomicReference<Throwable> ex = new AtomicReference<>();
    final AtomicReference<EventBus> eventBus = new AtomicReference<>();

    final DownloadStore downloadStore;
    //OutputStreamFactory osFactory;
    final DownloadStore.StoreKey destKey;
    final int maxSimulChunks = 1;
    final StreamManager streamManager;

    public RunnableTransfer(PeerDHT thisPeer, RemoteFile remoteFile, StoreKey destKey, DownloadStore downloadStore, StreamManager streamManager, ExecutorService execService, EventBus eventBus) {
        this.thisPeer = thisPeer;
        this.remoteFile = remoteFile;
        this.destKey = destKey;
        this.downloadStore = downloadStore;
        this.streamManager = streamManager;
        this.execService.set(execService);
        this.eventBus.set(eventBus);
        reset("Locating");
    }

    protected final void reset(String initStage) {
        this.currStage.set(initStage);
        DownloadCallable tc = new DownloadCallable();
        this.pauseFuture.set(new PausableFutureTask<>(tc));
        pauseFuture.get().unpause();
        //
    }

    protected void onStage() {
    }

    protected void onState() {
    }

    protected void onProgress(Range<Long> r) {
    }

    public String name() {
        if (remoteFile == null) {
            return null;
        }
        return remoteFile.getFullName();
    }

    public StoreKey destination() {
        return destKey;
    }

    
    @Override
    public RemoteFile resource() {
        return remoteFile;
    }

    public Throwable exception() {
        return ex.get();
    }

    @Override
    public Void call() throws RuntimeException {
        logger.debug("call");
        try {
            pauseFuture.get().run();
            return null;
        } catch (RuntimeException e) {
            e.printStackTrace();
            Throwable t = e.getCause();
            if (t != null) {
                ex.set(t);
            } else {
                ex.set(e);
            }
            throw e;
        } finally {
            logger.debug("done call");
            notifyStateChange();
        }
    }

    @Override
    public void unpause() {
        pauseFuture.get().unpause();
        notifyStateChange();
    }

    @Override
    public void pause() {
        pauseFuture.get().pause();
        notifyStateChange();
    }

    @Override
    public void cancel() {
        boolean b = pauseFuture.get().cancel(true);
        if (b) {
            notifyStateChange();
        }
    }

    @Override
    public void await() throws InterruptedException, ExecutionException {
        if (pauseFuture.get() == null) {
            return;
        }
        pauseFuture.get().get();
    }

    @Override
    public void restart(boolean resume) {
        setupForRestartOrResume(resume);
        execService.get().submit(this);
    }

    public void setupForRestartOrResume(boolean resume) {
        if (!isDone()) {
            logger.warn("Must finish or cancel before restart");
            return;
        }

        //Throwable t = exception.get();
        transferTracker.get().reset();
        ex.set(null);
        if (resume) {
            logger.info("Resuming transfer");
            reset(resumeStage.get());
        } else {
            logger.info("Restarting transfer");
            reset(restartStage.get());

        }

        started.set(false);
        onState();
    }

    @Override
    public boolean isDone() {
        return pauseFuture.get().isDone();
    }

    @Override
    public boolean isPaused() {
        return pauseFuture.get().isPaused();
    }

    @Override
    public boolean isRunning() {
        return !isPaused() && isStarted() && !isDone();
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    @Override
    public boolean isCancelled() {
        return pauseFuture.get().isCancelled();
    }

    //@Override
    public Object stage() {
        return stage;
    }

    @Override
    public void setEventBus(EventBus e) {
        eventBus.set(e);
    }

    @Override
    public TransferTracker tracker() {
        return transferTracker.get();
    }

    void dispatch(Object o) {
        eventBus.get().post(o);
    }

    void notifyRangeDone(Range<Long> range) {
        logger.debug("Range {} completed for file {}", range, remoteFile.getFullName());
        rangeTracker.get().updateSuccess(range);
        //
        transferTracker.get().update(range.upperEndpoint() - range.lowerEndpoint());
        onProgress(range);
        dispatch(new TransferEvent(null, range));
        //
        if (rangeTracker.get().getNeedRanges().isEmpty()) {
            // allDone.set(true);
            rangeTracker.get().updateDone();
            linesSemaphore.get().release(maxSimulChunks);
        } else {
            linesSemaphore.get().release();
        }
    }

    int MAX_CHUNK_FAILS = 3;
    int MAX_CONSEC_FAILS = 5;

    void notifyRangeFailed(Range<Long> range, Throwable thrwbl) throws InterruptedException {
        thrwbl.printStackTrace();
        logger.debug("Range {} failed for file {}: {} {}", range, remoteFile.getFullName(),
                thrwbl.getClass(), thrwbl.getMessage());
        rangeTracker.get().updateFailed(range);
        dispatch(null);
        //
        int consecFails = rangeTracker.get().getConsecFailures();
        int rangeFails = rangeTracker.get().getFailures(range);
        if (consecFails > MAX_CONSEC_FAILS) {
            ex.set(null);
            rangeTracker.get().updateDone();
            linesSemaphore.get().release(maxSimulChunks);

        } else if (rangeFails > MAX_CHUNK_FAILS) {
            ex.set(null);
            rangeTracker.get().updateDone();
            linesSemaphore.get().release(maxSimulChunks);
        } else {
            linesSemaphore.get().release();
        }

    }

    void notifyStageChange(String stage) {
        logger.debug("stage change: {}", stage);
        Long size = remoteFile.getLength();
        if (size != null && transferTracker.get().total() == null) {
            transferTracker.get().total(size);
        }
        onStage();
        dispatch(new TransferEvent(TransferEvent.EventType.STAGE_CHANGE, this));
    }

    void notifyStateChange() {
        TransferState ts = TransferState.of(this);
        logger.debug("state change: {}", ts);
        if (ts == TransferState.ERROR) {
            try {
                pauseFuture.get();
            } catch (Exception e) {
                logger.error("{}: {}", e.getClass(), e.getMessage());
                e.printStackTrace();
            }
        }
        if (isDone()) {
            transferTracker.get().done();
            rangeTracker.get().updateDone();
        }
        onState();
        dispatch(new TransferEvent(TransferEvent.EventType.STATE_CHANGE, this));
    }


    private class DownloadCallable<Void> implements PausableCallable<Void> {

        Pauser pauser;
        SeekableOutputStream fileOut = null;
        FuturePeerConnection peerConn = null;

        @Override
        public Pausable getPause() {
            return pauser;
        }

        @Override
        public Void call() throws Exception {
            int TCP_TIMEOUT = 30;
            int MAX_CHUNK_SIZE = 1024 * 1024 * 8;

            logger.debug("start call");

            try {
                //init
                this.pauser.checkAndBlock();
                transferTracker.set(new TransferTracker(eventManager.get(), remoteFile.getLength()));
                transferTracker.get().start(RunnableTransfer.this);
                started.set(true);
                notifyStateChange();
                //locate file
                PeerAddress remotePeer = remoteFile.getLocation(false);
                //create persistent direct connection
                this.pauser.checkAndBlock();
                peerConn = thisPeer.peer().createPeerConnection(remotePeer, TCP_TIMEOUT);
                //send want request
                this.pauser.checkAndBlock();
                RequestPacket wantPacket = new RequestPacket(Method.WANT, remoteFile.getId(), remoteFile.getLength());
                FutureDirect wantReq = thisPeer.peer().sendDirect(peerConn).object(wantPacket).start();
                //block for want response
                Object o = wantReq.await().object();
                ResponsePacket havePacket = getOkResponseOrThrowException(o);
                //start downloading chunks
                this.pauser.checkAndBlock();
                Range nextRange = null;
                while ((nextRange = rangeTracker.get().dequeuNextRange()) != null) {
                    checkException();
                    this.pauser.checkAndBlock();

                    linesSemaphore.get().acquire();
                    //check error
                    RequestPacket getPacket = new RequestPacket(Method.WANT, remoteFile.getId(), nextRange);
                    FutureDirect getReq = thisPeer.peer().sendDirect(peerConn).object(getPacket).start();
                    //add listener instead of block, to support later pipelining
                    getReq.addListener(new GiveHandler(nextRange));
                }
            } finally {
                if (peerConn != null) {
                    peerConn.close();
                }
            }
            
            //locate file
            //create connection
            //want request
            //start downloading chunks
            return null;
        }

        void writeRange(Range<Long> r, byte[] b) throws IOException {
            synchronized (downloadStore) {
                if (fileOut == null) {
                    fileOut = downloadStore.update(destKey);
                }
                ByteArrayInputStream bais = new ByteArrayInputStream(b);
                try (InputStream is = streamManager.createThrottledDownload(bais)) {
                    fileOut.seek(r.lowerEndpoint());
                    ByteStreams.copy(is, fileOut);
                }
            }
        }

        void checkException() throws Exception {
            Throwable t = ex.get();
            if (t != null) {
                if (t instanceof Exception) {
                    throw (Exception) t;
                } else {
                    throw new RuntimeException(t);
                }
            }
        }

        ResponsePacket getOkResponseOrThrowException(Object o) throws IOException {
            if (!(o instanceof ResponsePacket)) {
                throw new IOException("protocol exception");
            }
            ResponsePacket haveResp = (ResponsePacket) o;
            //handle error responses
            if (haveResp.getStatus() != Status.OK) {
                throw new IOException("Error status" + haveResp.getStatus());
            }
            return haveResp;
        }

        class GiveHandler implements BaseFutureListener<FutureDirect> {

            Range<Long> range;

            GiveHandler(Range<Long> range) {
                this.range = range;
            }

            @Override
            public void operationComplete(FutureDirect f) throws Exception {
                Object o = f.object();
                ResponsePacket resp = getOkResponseOrThrowException(o);
                switch (resp.getStatus()) {
                    case OK:
                        notifyRangeDone(range);
                        break;
                    case ERROR:
                        notifyRangeFailed(range, resp.getException());
                        break;
                    default:
                        IOException e = new IOException("Response packet error code: " + resp.getStatus() + " " + resp.getMessage());
                        notifyRangeFailed(range, e);
                }
                //put back in queue
                //linesSemaphore.release();
            }

            @Override
            public void exceptionCaught(Throwable thrwbl) throws Exception {
                notifyRangeFailed(range, thrwbl);

                //put back in queue
                //count retries, throw exception
                //linesSemaphore.release();
            }

        }
    }

    
}
