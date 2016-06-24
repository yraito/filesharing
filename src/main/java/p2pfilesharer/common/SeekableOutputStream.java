package p2pfilesharer.common;


import videodownloader.util.*;
import java.io.IOException;
import java.io.OutputStream;


/**
 *
 * @author Nick
 */
public abstract class SeekableOutputStream extends OutputStream {

    public abstract long size() throws IOException;
    
    public abstract void seek(long pos) throws IOException;
}
