/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.transfer;

import java.io.IOException;
import p2pfilesharer.common.SeekableInputStream;

/**
 *
 * @author Edward
 */
public interface LocalFile extends File{
    
    SeekableInputStream getContent() throws IOException;
}
