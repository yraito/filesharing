
package p2pfilesharer.publish.impl;

import net.tomp2p.peers.Number160;

/**
 *
 * @author Nick
 */
public class DhtLocation {
    
    public final Number160 domainKey;
    public final Number160 locationKey;

    public DhtLocation(Number160 domainKey, Number160 locationKey) {
        this.domainKey = domainKey;
        this.locationKey = locationKey;
    }

    @Override
    public String toString() {
        return "DhtLocation{" + "domainKey=" + domainKey + ", locationKey=" + locationKey + '}';
    }
    
    
}
