
package p2pfilesharer.transfer.impl;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

/**
 *
 * @author Nick
 */
public class ResponsePacket {
 
    public static ResponsePacket success(Range r, byte[] data) {
        return new ResponsePacket(Status.OK, null, r, data, null, null);
    }
    
    public static ResponsePacket success(RangeSet r) {
        return new ResponsePacket(Status.OK, r, null, null, null, null);
    }
    
    public static ResponsePacket failure(Status s, RangeSet r) {
        return new ResponsePacket(s, r, null, null, null, null);
    }
     
    public static ResponsePacket failure(Status s, RangeSet r, String m) {
        return new ResponsePacket(s, r, null, null, m, null);
    }
    
    public static ResponsePacket failure(Exception e) {
        return new ResponsePacket(Status.ERROR, null, null, null, null, e);
    }
    
    public enum Status {
        OK, MISSING_SOME, NOT_FOUND, NO_PERMISSION, ERROR
    }

    private ResponsePacket(Status status, RangeSet haveRanges, Range range, byte[] data, String message, Exception exception) {
        this.status = status;
        this.haveRanges = haveRanges;
        this.range = range;
        this.data = data;
        this.exception = exception;
    }

    Status status;
    RangeSet haveRanges;
    Range range;
    Exception exception;
    byte[] data;
    String message;
    
    public Status getStatus() {
        return status;
    }

    public RangeSet getHaveRanges() {
        return haveRanges;
    }

    public Range getRange() {
        return range;
    }

    public Exception getException() {
        return exception;
    }

    public byte[] getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
    
    
}
