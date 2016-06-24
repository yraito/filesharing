
package p2pfilesharer.publish.impl;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

/**
 * Object that is published to the network to advertise the presence of a peer
 * 
 * @author Nick
 */
public class PeerInfo {
    
    public final static String LOCATION_KEY = "PeerList";
    
    public final String name;
    public final PeerAddress peerAddress;

    public final Number160 indexPeerId;
    
    public PeerInfo(String name, PeerAddress peerAddress) {
        this.name = name;
        this.peerAddress = peerAddress;
        this.indexPeerId = peerAddress.peerId();
    }
       
}
