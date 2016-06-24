
package p2pfilesharer.transfer.impl;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import net.tomp2p.peers.Number160;

/**
 *
 * @author Nick
 */
public class RequestPacket {
    
    public enum Method {
        WANT, GET
    }
    
    private final Method method;
    private final Number160 fileId;
    private final Range<Long> wantedRange;

    public RequestPacket(Method method, Number160 fileId, Range<Long> wantedRange) {
        this.method = method;
        this.fileId = fileId;
        this.wantedRange = wantedRange;
    }

    public RequestPacket(Method method, Number160 fileId, long fileSize) {
        this.method = method;
        this.fileId = fileId;
        this.wantedRange = Range.closedOpen(0L, fileSize);
    }

    public Method getMethod() {
        return method;
    }

    public Number160 getFileId() {
        return fileId;
    }

    public Range getWantedRange() {
        return wantedRange;
    }

    @Override
    public String toString() {
        return "RequestPacket{" + "method=" + method + ", fileId=" + fileId + ", wantedRange=" + wantedRange + '}';
    }
    
}
