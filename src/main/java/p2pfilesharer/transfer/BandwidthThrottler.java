package p2pfilesharer.transfer;

/**
 *
 * @author Nick
 */
public interface BandwidthThrottler {
    
    /**.
     * @param bytesPerSec the max upload speed in bytes/sec, or 0 to disable upload throttling
     */
    void setMaxUploadSpeed(long bytesPerSec);
    
    /**
     * @param bytesPerSec the max download speed in bytes/sec, or 0 to disable download throttling 
     */
    void setMaxDownloadSpeed(long bytesPerSec);
}
