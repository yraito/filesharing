package p2pfilesharer.transfer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Edward
 */
public interface LocalFileFactory {
    
    LocalFile getResource(String path) throws DaoException;
}
