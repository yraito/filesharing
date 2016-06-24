package p2pfilesharer.transfer;


import java.io.IOException;


/**
 *
 * @author Nick
 */
public interface TransferStage {
    
    String description();
    
    TransferStage next();
    
    Long execute() throws IOException, InterruptedException;
    
    
}
