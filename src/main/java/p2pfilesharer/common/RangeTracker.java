/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.common;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


/**
 *
 * @author Edward
 */
public class RangeTracker {

    final static Range<Long> DONE_RANGE = Range.closedOpen(-1L, 0L);
    
    int MAX_CHUNK_SIZE = 1024 * 1024 * 8;
    
    RangeSet<Long> allRanges;
    RangeSet<Long> haveRanges;
    RangeSet<Long> needRanges;
    Set<Range<Long>> activeRanges = ConcurrentHashMap.newKeySet();
    BlockingDeque<Range<Long>> rangeQueue = new LinkedBlockingDeque<>();
    AtomicInteger consecFailures = new AtomicInteger(0);
    Map<Range<Long>, Integer> rangeFailures = new ConcurrentHashMap<>();
    AtomicBoolean allDone = new AtomicBoolean(false);

    public RangeTracker(RangeSet<Long> allRanges) {
        this(allRanges, TreeRangeSet.create());
    }

    public RangeTracker(RangeSet<Long> allRanges, RangeSet<Long> haveRanges) {
        this.allRanges = allRanges;
        this.haveRanges = haveRanges;
        this.needRanges = TreeRangeSet.create(allRanges);
        this.needRanges.removeAll(haveRanges);
        if (needRanges.isEmpty()) {
            allDone.set(true);
        }
        
    }
    
    public Range<Long> dequeuNextRange() throws InterruptedException {
        if (allDone.get()) {
            return null;
        }
        Range<Long> r = rangeQueue.take();
        if (r == DONE_RANGE || allDone.get()) { // just check all done instead?
            return null;//throw interrupted exception instead? or execution exception?
        }
        long size = r.upperEndpoint() - r.lowerEndpoint();
        if (size > MAX_CHUNK_SIZE) {
            Range rTake = Range.closedOpen(r.lowerEndpoint(), r.lowerEndpoint() + MAX_CHUNK_SIZE);
            Range rPutBack = Range.closedOpen(r.lowerEndpoint() + MAX_CHUNK_SIZE, r.upperEndpoint());
            rangeQueue.addFirst(rPutBack);
            r = rTake;
        }
        activeRanges.add(r);
        return r;
    }

    //requeue
    public void updateFailed(Range<Long> r) {
        rangeQueue.addLast(r);
        activeRanges.remove(r);
        consecFailures.incrementAndGet();
        rangeFailures.put(r, 1 + rangeFailures.getOrDefault(r, 0));
    }

    public void updateSuccess(Range<Long> r) {
        haveRanges.add(r);
        needRanges.remove(r);
        activeRanges.remove(r);
        consecFailures.set(0);
        if (needRanges.isEmpty()) {
            updateDone();
        }
    }

    public void updateDone() {
        allDone.set(true);
        rangeQueue.offer(DONE_RANGE);
        IntStream.iterate(0, i -> i + 1).limit(activeRanges.size()).forEach(i -> {
            rangeQueue.offer(DONE_RANGE);
        });
    }

    public int getConsecFailures() {
        return consecFailures.get();
    }

    public int getFailures(Range<Long> r) {
        return rangeFailures.getOrDefault(r, 0);
    }

    public boolean isDone() {
        return allDone.get();
    }

    public RangeSet<Long> getHaveRanges() {
        return haveRanges;
    }

    public RangeSet<Long> getNeedRanges() {
        return needRanges;
    }

    public Set<Range<Long>> getActiveRanges() {
        return activeRanges;
    }
    
    

}
