/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.p2px;

import java.io.IOException;
import net.tomp2p.storage.Data;

/**
 *
 * @author Edward
 */
public class DataX extends Data{
    
    public static Data unchecked(Object o) {
        try {
            return new Data(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
