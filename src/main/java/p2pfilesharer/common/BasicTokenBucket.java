package p2pfilesharer.common;


import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Edward
 */
public class BasicTokenBucket implements TokenBucket {

    // private final Semaphore tokensInBucket = new Semaphore(0, true);
    // private Semaphore bucketSemaphore;
    // private Lock bucketLock;
    private final ReentrantLock bucketLock = new ReentrantLock(true);
    private final ReentrantLock configLock = new ReentrantLock();
    private final Condition configCondition = configLock.newCondition();
    private long tokenCount;
    private long bucketCapacity;
    private long fillRate;
    private TimeUnit fillUnit;
    private long lastFill = System.nanoTime();
    //private final AtomicDouble capacity = new AtomicDouble();
    //private final AtomicDouble fillRate = new AtomicDouble();
    //private final AtomicReference<TimeUnit> fillUnit = new AtomicReference<>();
    //private final AtomicLong lastFill = new AtomicLong();

    public BasicTokenBucket(long bucketCapacity, long fillRate, TimeUnit fillUnit) {
        setCapacity(bucketCapacity);
        setFillRate(fillRate, fillUnit);
    }
    
    @Override
    public long getCapacity() {
        configLock.lock();
        try {
            return bucketCapacity;
        } finally {
            configLock.unlock();
        }
    }
    
    @Override
    public final void setCapacity(long num) {
        if (num <= 0) {
            throw new IllegalArgumentException("Token bucket can't have non-positive capacity: " + num);
        }

        configLock.lock();
        try {
            bucketCapacity = num;
        } finally {
            configLock.unlock();
        }
    }

    @Override
    public final void setFillRate(long num, TimeUnit timeUnit) {
        if (num <= 0) {
            throw new IllegalArgumentException("Token bucket can't have non-positive fill rate: " + num);
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("Token bucket can't have null fill rate unit");
        }

        configLock.lock();
        try {
            fillRate = num;
            fillUnit = timeUnit;
            configCondition.signalAll();
        } finally {
            configLock.unlock();
        }
    }

    @Override
    public void takeBlocking(int num) throws InterruptedException {
        if (num <= 0) {
            throw new IllegalArgumentException("Cannot take a non-positive number of tokens");
        }

        long tokensAcquired = 0;

        try {
            bucketLock.lockInterruptibly();
            configLock.lockInterruptibly();
            refill();

            while (tokensAcquired < num) {
                tokensAcquired += Math.min(num, tokenCount);
                tokenCount -= tokensAcquired;
                if (tokensAcquired < num) {
                    long tokensToFill = Math.min(bucketCapacity, num - tokensAcquired);
                    long sleepTime = getTokenPeriodNanos() * tokensToFill;
                    configCondition.awaitNanos(sleepTime);
                    refill();
                }
            }

        } catch (InterruptedException e) {
            tokenCount += tokensAcquired;
            throw e;
        } finally {
            if (configLock.isHeldByCurrentThread()) {
                configLock.unlock();
            }
            if (bucketLock.isHeldByCurrentThread()) {
                bucketLock.unlock();
            }
        }

    }

    @Override
    public boolean tryTake(int num) {

        if (num <= 0) {
            throw new IllegalArgumentException("Cannot take a non-positive number of tokens");
        }

        if (!bucketLock.tryLock()) {
            return false;
        }

        try {
            refill();
            if (tokenCount >= num) {
                tokenCount -= num;
                return true;
            }
            return false;

        } finally {
            bucketLock.unlock();
        }
    }

    private void refill() {
        long currTime = System.nanoTime();
        long prevTime = lastFill;
        long elapsedNanos = currTime - prevTime;
        long tokenPeriod = getTokenPeriodNanos();
        if (elapsedNanos > tokenPeriod) {
            long tokensAdded = (long) (elapsedNanos / tokenPeriod);
            lastFill = (long) (tokensAdded * tokenPeriod);
            tokenCount += tokensAdded;
        }
    }

    private long getTokenPeriodNanos() {
        long nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, fillUnit);
        long tokenPeriodNanos = nanosPerUnit / fillRate;
        return tokenPeriodNanos;
    }
}
