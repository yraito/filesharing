
package p2pfilesharer.publish.impl.search;

import java.io.IOException;
import java.util.NavigableMap;
import java.util.Set;
import java.util.stream.Collectors;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import p2pfilesharer.transfer.Interceptor;
import p2pfilesharer.transfer.InterceptorChain;

/**
 *
 * @author Nick
 */
public class QueryResolvingInterceptor implements Interceptor {

    private final static Logger logger = LoggerFactory.getLogger(QueryResolvingInterceptor.class);
   
    @Override
    public Object reply(Object m, PeerDHT thisPeer, PeerAddress thatPeer, InterceptorChain chain) {
        if (!(m instanceof QueryPacket)) {
            return chain.nextInterceptor(thisPeer, thatPeer, m);
        }

        QueryPacket reqPacket = (QueryPacket) m;
        logger.debug("Handling query packet {}", reqPacket);
        Number640 lwrBound = new Number640(
                reqPacket.getLocationKey(), 
                reqPacket.getDomainKey(), 
                Number160.ZERO,
                Number160.ZERO);
        Number640 upprBound = new Number640(
                reqPacket.getLocationKey(),
                reqPacket.getDomainKey(),
                Number160.MAX_VALUE,
                Number160.MAX_VALUE);
       
        logger.debug("Looking up local storage, keys {} to {}", lwrBound, upprBound);
        NavigableMap<Number640, Data> dataMap = thisPeer
                .storageLayer()
                .get(lwrBound, upprBound, -1, true);
        Set<?> resultSet = dataMap.entrySet()
                .stream()
                .map(e -> {
                    try {
                        return e.getValue().object();
                    } catch (ClassNotFoundException | IOException ex) {
                        logger.error("Exception deserializing object: {}", ex);
                        return ex;
                    }
                })
                .filter(e -> reqPacket.getMatchesPredicate().test(e))
                .limit(reqPacket.getLimit())
                .collect(Collectors.toSet());
        logger.debug("{} matching keys. Returning {} results", dataMap.size(), resultSet.size());
        return new ResultSetPacket(resultSet);
    }   

}
