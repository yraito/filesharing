package p2pfilesharer.transfer;


import p2pfilesharer.transfer.InterceptorChain;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.PeerAddress;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Edward
 */
public interface Interceptor {
   
    Object reply(Object m, PeerDHT thisPeer, PeerAddress thatPeer, InterceptorChain chain);
}
