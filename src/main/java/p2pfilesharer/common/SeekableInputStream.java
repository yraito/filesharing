package p2pfilesharer.common;


import java.io.IOException;
import java.io.InputStream;


/**
 *
 * @author Nick
 */
public abstract class SeekableInputStream extends InputStream {
    
    public abstract long size() throws IOException;
    
    public abstract void seek(long pos) throws IOException;
}
