
package p2pfilesharer.common;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Nick
 */
public class PausableFutureTask<V> implements Runnable, Pausable, ListenableFuture<V>{

    final PausableCallable<V> callable;
    final ListenableFutureTask<V> future;

    public PausableFutureTask(PausableCallable<V> callable) {
        this.callable = callable;
        this.future = ListenableFutureTask.create(callable);
    }
    
    @Override
    public void pause() {
        callable.getPause().pause();
    }

    @Override
    public void unpause() {
        callable.getPause().unpause();
    }

    @Override
    public boolean isPaused() {
        return callable.getPause().isPaused();
    }

    @Override
    public void addListener(Runnable r, Executor exctr) {
        future.addListener(r, exctr);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }
    
    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public void run() {
        future.run();
    }
    
}
