package p2pfilesharer.transfer;

import java.io.IOException;


/**
 *
 * @author Nick
 */
public class DaoException extends IOException{
    
    public DaoException(String message) {
        super(message);
    }
    
    public DaoException(Throwable cause) {
        super(cause);
    }
    
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
