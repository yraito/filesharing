/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.transfer;

import net.tomp2p.peers.Number160;

/**
 *
 * @author Edward
 */
public interface File {
    
    Number160 getId();
    
    String getFullName();
    
    String getNameWithoutExt();
    
    String getExtension();
    
    Long getLength();

}
