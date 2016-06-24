package p2pfilesharer.common;


import java.util.concurrent.TimeUnit;


/**
 *
 * @author Nick
 */
public class Duration {
    
    final TimeUnit timeUnit;
    final long num;
    
    public Duration(TimeUnit timeUnit, long num) {
        long finalNum = num;
        TimeUnit finalUnit = timeUnit;
        for (TimeUnit tu : TimeUnit.values()) {
            long thisNum = tu.convert(num, timeUnit);
            if (thisNum > 0 && thisNum < finalNum) {
                finalNum = thisNum;
                finalUnit = tu;
            }
        }
        this.timeUnit = finalUnit;
        this.num = finalNum;
    }
    
    public Duration(long numMillis) {
        this(TimeUnit.MILLISECONDS, numMillis);
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getNum() {
        return num;
    }
    
    public String toString() {
        return num + " " + timeUnit.toString();
    }
    
    
    
}
