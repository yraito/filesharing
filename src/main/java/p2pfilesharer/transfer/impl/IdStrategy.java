
package p2pfilesharer.transfer.impl;

import java.io.IOException;
import java.nio.file.Path;
import net.tomp2p.peers.Number160;
import net.tomp2p.dht.PeerDHT;
/**
 *
 * @author Nick
 * 
 */
public interface IdStrategy {
    
   Number160 generateId(Path filePath, PeerDHT thisPeer) throws IOException;
}
