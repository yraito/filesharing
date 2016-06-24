package p2pfilesharer.publish.impl.search;


import java.util.function.Predicate;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number320;


/**
 *
 * @author Nick
 */
public class QueryPacket {
    
    final Number160 locationKey;
    final Number160 domainKey;
    final Predicate matchesPredicate;
    final int limit;

    public QueryPacket(Number160 locationKey, Number160 domainKey, Predicate matchesPredicate, int limit) {
        this.locationKey = locationKey;
        this.domainKey = domainKey;
        this.matchesPredicate = matchesPredicate;
        this.limit = limit;
    }
    
    
    
    //public Set<?> searchLocalStorage(PeerDHT p) {
    //    p.peer().
    //}


    public Predicate getMatchesPredicate() {
        return matchesPredicate;
    }

    public int getLimit() {
        return limit;
    }

    public Number160 getLocationKey() {
        return locationKey;
    }

    public Number160 getDomainKey() {
        return domainKey;
    }
   
    public Number320 getLocationAndDomainKey() {
        return new Number320(locationKey, domainKey);
    }
}
