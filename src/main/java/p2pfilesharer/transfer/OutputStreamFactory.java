package p2pfilesharer.transfer;


import p2pfilesharer.common.KeyedSeekableOutputStream;
import java.io.IOException;

/**
 * 
 * @author Nick
 */
public interface OutputStreamFactory {
    
    KeyedSeekableOutputStream createOutputStream(RemoteFile remoteFile) throws IOException;
    
}
