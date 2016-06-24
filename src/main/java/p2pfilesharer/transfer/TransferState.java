package p2pfilesharer.transfer;


import java.util.concurrent.ExecutionException;


/**
 *
 * @author Nick
 */
public enum TransferState {
    
    QUEUED, RUNNING, PAUSED, COMPLETED, CANCELLED, ERROR;
    
    public static TransferState of(Transfer tf) {
        if (tf.isDone()) {
            if (tf.isCancelled()) {
                return CANCELLED;
            }
            try {
                tf.await();
                return COMPLETED;
            } catch (ExecutionException e) {
                return ERROR;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (tf.isPaused()) {
                return PAUSED;
            }
            if (!tf.isStarted()) {
                return QUEUED;
            }
            return RUNNING;
        }
    }
}
