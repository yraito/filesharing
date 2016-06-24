package p2pfilesharer.transfer;


import com.google.common.eventbus.EventBus;
import java.util.concurrent.ExecutionException;
import p2pfilesharer.common.TransferTracker;

/**
 *
 * @author Nick
 */
public interface Transfer {
    
    File resource();
    TransferTracker tracker();
    Object stage();
    void await() throws ExecutionException, InterruptedException;
    void pause();
    void unpause();
    void cancel();
    void restart(boolean tryResume);
    boolean isStarted();
    boolean isPaused();
    boolean isCancelled();
    boolean isRunning();
    boolean isDone();
    void setEventBus(EventBus e);
}
