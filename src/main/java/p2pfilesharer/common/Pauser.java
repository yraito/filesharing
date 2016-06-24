package p2pfilesharer.common;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Nick
 */
public class Pauser implements Pausable{

    private final static Logger logger = LoggerFactory.getLogger(Pauser.class);
    private final AtomicBoolean paused = new AtomicBoolean();
    //private final AtomicBoolean done = new AtomicBoolean();
    //private final Runnable callback;

    private final Lock lock;
    private final Condition condition;
    
    public Pauser() {
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }
    
    @Override
    public synchronized void pause() {
        paused.set(true);
    }

    @Override
    public synchronized void unpause() {
        paused.set(false);
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }


    @Override
    public synchronized boolean isPaused() {
        return paused.get();
    }

    public void checkAndBlock() throws InterruptedException {
        checkInterrupted();
        blockUntilResume();
        checkInterrupted();
    }

    public void checkInterrupted() throws InterruptedException {
        logger.trace("checking if interrupted/cancelled");
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        logger.trace("not interrupted/cancelled");
    }

    public void blockUntilResume() throws InterruptedException {
        logger.trace("Waiting until unpaused. Paused: {}", isPaused());
        lock.lock();
        try {
            while (isPaused()) {
                logger.trace("Entering wait loop. isPaused: {}", paused.get());
                condition.await();
            }
        } finally {
            lock.unlock();
        }

        logger.trace("Not paused, exiting wait");
    }

}
