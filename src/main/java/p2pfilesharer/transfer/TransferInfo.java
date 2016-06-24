/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.transfer;

import java.util.concurrent.atomic.AtomicReference;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FuturePeerConnection;

/**
 *
 * @author Edward
 */
public class TransferInfo {
//PeerAddress remotePeer, ranges, file
    RemoteFile remoteFile;
    PeerDHT thisPeer;
    DownloadStore fileDao;
    OutputStreamFactory osFactory;
    Object destKey;
}
