

package p2pfilesharer.transfer;

import p2pfilesharer.common.SeekableOutputStream;

/**
 *
 * @author Nick
 */
public interface DownloadStore {
    
    interface StoreKey {
        String getDest();
    }
    
    StoreKey create(RemoteFile file) throws DaoException;
    
    SeekableOutputStream update(StoreKey id) throws DaoException;
    
    boolean finish(StoreKey id) throws DaoException;
    
    boolean delete(StoreKey id) throws DaoException;
}
