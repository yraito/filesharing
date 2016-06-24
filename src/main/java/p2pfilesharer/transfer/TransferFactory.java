
package p2pfilesharer.transfer;

import com.google.common.collect.RangeSet;
import java.io.IOException;
import net.tomp2p.dht.PeerDHT;

/**
 *
 * @author Nick
 */
public interface TransferFactory {
    
    Transfer download(PeerDHT thisPeer, RemoteFile file, DownloadStore fileDao, RangeSet<Long> range) throws IOException;
}
