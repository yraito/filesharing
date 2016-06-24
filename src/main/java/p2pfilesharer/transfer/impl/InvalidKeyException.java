package p2pfilesharer.transfer.impl;

import p2pfilesharer.transfer.DaoException;


/**
 *
 * @author Nick
 */
public class InvalidKeyException extends DaoException {

    public InvalidKeyException(String message) {
        super(message);
    }

    public InvalidKeyException(Throwable cause) {
        super(cause);
    }

    public InvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }

}
