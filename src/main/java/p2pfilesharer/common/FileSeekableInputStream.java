package p2pfilesharer.common;


import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;


/**
 *
 * @author Nick
 */
public class FileSeekableInputStream extends SeekableInputStream {

    FileChannel fc;
    InputStream is;

    public FileSeekableInputStream(FileChannel fc) {
        this.fc = fc;
        this.is = Channels.newInputStream(fc);
    }
    
    @Override
    public long size() throws IOException{
        return fc.size();
    }

    @Override
    public void seek(long pos) throws IOException {
        fc.position(pos);
    }

    @Override
    public int read() throws IOException {
       return is.read();
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        return is.read(b, off, len);
    }
    
    @Override
    public void close() throws IOException {
        fc.close();
    }
    
}
