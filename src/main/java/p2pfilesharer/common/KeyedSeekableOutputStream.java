
package p2pfilesharer.common;

import java.io.IOException;

/**
 *
 * @author Nick
 */
public class KeyedSeekableOutputStream extends SeekableOutputStream {
    
    final SeekableOutputStream sos;
    final Object key;

    public KeyedSeekableOutputStream(SeekableOutputStream sos, Object key) {
        this.sos = sos;
        this.key = key;
    }

    public Object key() {
        return key;
    }

    @Override
    public long size() throws IOException {
        return sos.size();
    }

    @Override
    public void seek(long pos) throws IOException {
        sos.seek(pos);
    }

    @Override
    public void write(int b) throws IOException {
        sos.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        sos.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        sos.flush();
    }

    @Override
    public void close() throws IOException {
        sos.close();
    }
    
    
    
}
