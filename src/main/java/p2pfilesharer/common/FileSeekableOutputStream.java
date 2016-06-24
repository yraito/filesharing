package p2pfilesharer.common;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Nick
 */
public class FileSeekableOutputStream extends SeekableOutputStream {

    private final FileChannel fc;
    private final OutputStream os;

    public FileSeekableOutputStream(Path p) throws IOException{
        this.fc = FileChannel.open(p, StandardOpenOption.WRITE);
        this.os = Channels.newOutputStream(fc);
    }
    
    @Override
    public long size() throws IOException {
        return fc.size();
    }

    @Override
    public void seek(long pos) throws IOException {
        fc.position(pos);
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }
    
    @Override
    public void close() throws IOException {
        os.close();
    }
    
}
