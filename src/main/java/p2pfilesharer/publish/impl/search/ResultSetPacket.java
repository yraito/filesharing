/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.publish.impl.search;

import java.util.Set;

/**
 *
 * @author Edward
 */
public class ResultSetPacket {
    
    final Set<?> resultSet;

    public ResultSetPacket(Set<?> resultSet) {
        this.resultSet = resultSet;
    }
            
    public Set<?> getResultSet() {
        return resultSet;
    }
}
