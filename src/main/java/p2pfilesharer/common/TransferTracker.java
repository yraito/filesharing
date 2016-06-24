package p2pfilesharer.common;

import com.google.common.eventbus.EventBus;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import p2pfilesharer.transfer.Transfer;
import p2pfilesharer.transfer.TransferEvent;
import p2pfilesharer.transfer.TransferEvent.EventType;
import p2pfilesharer.transfer.TransferState;


/**
 *
 * @author Nick
 */
public class TransferTracker {
//was static... create new TransferTracker not synchronized, so referencing shutdown sese, 
    //as doesnt wait until sese set to new executoraa
    ScheduledExecutorService sese;

    Transfer transfer;
    final EventBus eventBus;
    Bytes totalBytes;
    Bytes bytesDownloaded = new Bytes(0);
    Bytes downloadRate = new Bytes(0);
    final Queue<Progress> queue = new ArrayDeque<>(50);
    final Pauser pauser = new Pauser();
    Progress startProgress;
    
    private synchronized ScheduledExecutorService getExecutor() {
        return sese;
    }

    private synchronized void setExecutor(ScheduledExecutorService sese) {
        this.sese =sese;
        
    }
     public TransferTracker(EventBus eventManager, Long totalBytes) {
        this.eventBus = eventManager;
        if (totalBytes != null) {
            this.totalBytes = new Bytes(totalBytes);
        }
        //this.startTime = System.currentTimeMillis();
    }

    public synchronized void start(Transfer transfer) {
        this.transfer = transfer;
        this.startProgress = new Progress(new Bytes(0));
        setExecutor(Executors.newSingleThreadScheduledExecutor());
        queue.add(startProgress);
        Runnable r = () -> {
            try {
                pauser.checkAndBlock();
                update(0, true);
                eventBus.post(new TransferEvent(EventType.PROGRESS, transfer));
            } catch (InterruptedException e) {
                //
            }

        };
        getExecutor().scheduleAtFixedRate(r, 0, 1, TimeUnit.SECONDS);
    }

    public synchronized boolean isStarted() {
        return transfer != null;
    }

    public synchronized void pause() {
        pauser.pause();
    }

    public synchronized void unpause() {
        if (pauser.isPaused()) {
            queue.clear();
            queue.add(startProgress);
            pauser.unpause();
        }
        
    }

    public synchronized void done() {
        getExecutor().shutdownNow();
    }

    public synchronized void update(long deltaBytes) {
        update(deltaBytes, false);
    }

    private synchronized void update(long deltaBytes, boolean updateQueue) {
        if (!isStarted()) {
            throw new IllegalStateException("Not started");
        }
        bytesDownloaded = bytesDownloaded.plus(new Bytes(deltaBytes));
        if (updateQueue) {
            Progress progress = null;
            if (queue.size() >= 20) {
                progress = queue.remove();
            } else {
                progress = queue.element();
            }
            Progress currProgress = new Progress(bytesDownloaded);
            Bytes bytesDiff = currProgress.bytesSoFar.minus(progress.bytesSoFar);
            long timeDiff = currProgress.time - progress.time;
            if (timeDiff == 0) {
                downloadRate = new Bytes(Long.MAX_VALUE);
            } else {
                long bytesPerSecond = (long) (bytesDiff.getBytes() / timeDiff * 1000);
                downloadRate = new Bytes(bytesPerSecond);
            }
            queue.add(currProgress);
        }
    }

    public synchronized void reset() {
        reset(0L);
    }

    public synchronized void reset(long bytesSoFar) {
        getExecutor().shutdownNow();
        setExecutor(Executors.newSingleThreadScheduledExecutor());
        queue.clear();
        bytesDownloaded = new Bytes(bytesSoFar);
        downloadRate = new Bytes(0);
        this.transfer = null;
    }

    //void initial(long bytes)
    public synchronized void total(long bytes) {
        totalBytes = new Bytes(bytes);
    }

    public synchronized Bytes total() {
        return totalBytes;
    }

    public synchronized Bytes progress() {
        return bytesDownloaded;
    }

    public synchronized int progressPercent() {
        if (totalBytes == null) {
            return -1;
        }
        if (bytesDownloaded == null) {
            return 0;
        }
        double fraction = ((double) bytesDownloaded.getBytes()) / ((double) totalBytes.getBytes());
        return (int) (fraction * 100);
    }

    public synchronized Bytes rate() {
        return downloadRate;
    }

    public synchronized Duration timeLeft() {
        if (totalBytes == null) {
            return null;
        }
        if (totalBytes.getBytes() == bytesDownloaded.getBytes()) {
            return new Duration(TimeUnit.SECONDS, 0L);
        }
        if (transfer == null) {
            return null;
        }
        TransferState ts = TransferState.of(transfer);
        if (ts == TransferState.COMPLETED) {
            return new Duration(TimeUnit.SECONDS, 0L);
        }
        Bytes bytesLeft = totalBytes.minus(bytesDownloaded);
        long seconds = bytesLeft.dividedBy(rate());
        return new Duration(TimeUnit.SECONDS, seconds);
    }

    private static class Progress {

        final long time;
        final Bytes bytesSoFar;

        Progress(long time, Bytes bytesSoFar) {
            this.time = time;
            this.bytesSoFar = bytesSoFar;
        }

        Progress(Bytes bytes) {
            this.time = System.nanoTime()/1000000;
            this.bytesSoFar = bytes;
        }
    }
}
