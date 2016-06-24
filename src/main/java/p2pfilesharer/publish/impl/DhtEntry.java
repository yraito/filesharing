
package p2pfilesharer.publish.impl;

import net.tomp2p.peers.Number160;

/**
 *
 * @author Nick
 */
public class DhtEntry<K> {
    
    public final K obect;
    public final Number160 idKey;
    public final Number160 domainKey;
    public final Number160 locationKey;

    public DhtEntry(K obect, Number160 idKey, Number160 domainKey, Number160 locationKey) {
        this.obect = obect;
        this.idKey = idKey;
        this.domainKey = domainKey;
        this.locationKey = locationKey;
    }

    @Override
    public String toString() {
        return "DhtEntry{" + "clazz=" + obect.getClass() + ", idKey=" + idKey + ", domainKey=" + domainKey + ", locationKey=" + locationKey + '}';
    }
   
    
}
