/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.transfer;

import java.io.IOException;
import net.tomp2p.peers.PeerAddress;

/**
 *
 * @author Edward
 */
public interface RemoteFile extends File{
    
    PeerAddress getLocation(boolean useCache) throws IOException;
}
