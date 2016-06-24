
package p2pfilesharer.transfer.impl;

import com.google.common.io.Files;
import java.io.IOException;
import java.nio.file.Path;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import p2pfilesharer.transfer.File;
import p2pfilesharer.transfer.RemoteFile;

/**
 *
 * @author Nick
 */
public class BasicRemoteFile implements RemoteFile {

    protected final Path filePath;
    protected final Number160 fileId;
    protected final long fileLength;
    protected final PeerAddress peerAddress;
    
    public BasicRemoteFile(Path filePath, Number160 fileId, long fileLength, PeerAddress peerAddress) {
        this.filePath = filePath;
        this.fileId = fileId;
        this.fileLength = fileLength;
        this.peerAddress = peerAddress;
    }
    
    public BasicRemoteFile(Path filePath, File thatFile, PeerAddress peerAddress) {
        this.filePath = filePath;
        this.fileId = thatFile.getId();
        this.fileLength = thatFile.getLength();
        this.peerAddress = peerAddress;
    }
    
    @Override
    public Number160 getId() {
        return fileId;
    }

    @Override
    public String getFullName() {
        return filePath.getFileName().toString();
    }

    @Override
    public String getNameWithoutExt() {
        return Files.getNameWithoutExtension(getFullName());
    }

    @Override
    public String getExtension() {
        return Files.getFileExtension(getFullName());
    }

    @Override
    public Long getLength() {
        return fileLength;
    }

    @Override
    public PeerAddress getLocation(boolean useCache) throws IOException {
        return peerAddress;
    }
    
}
