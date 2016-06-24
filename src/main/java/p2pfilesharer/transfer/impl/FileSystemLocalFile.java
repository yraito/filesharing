
package p2pfilesharer.transfer.impl;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import p2pfilesharer.transfer.LocalFile;
import p2pfilesharer.common.FileSeekableInputStream;
import p2pfilesharer.common.SeekableInputStream;

/**
 *
 * @author Nick
 */
public class FileSystemLocalFile implements LocalFile {

    protected final Path filePath;
    protected final long fileLength;
    protected final PeerDHT thisPeer;
    protected final IdStrategy idStrategy;
    protected Number160 fileId;

    public FileSystemLocalFile(Path filePath, long fileLength, PeerDHT thisPeer, IdStrategy idStrategy) {
        this.filePath = filePath;
        this.fileLength = fileLength;
        this.thisPeer = thisPeer;
        this.idStrategy = idStrategy;
    }

    @Override
    public SeekableInputStream getContent() throws IOException {
        FileChannel fc = FileChannel.open(filePath, StandardOpenOption.READ);
        return new FileSeekableInputStream(fc);
    }

    @Override
    public synchronized Number160 getId() {
        try {
            if (fileId == null) {
                fileId = idStrategy.generateId(filePath, thisPeer);
            }
            return fileId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getFullName() {
        return filePath.getFileName().toString();
    }

    @Override
    public String getNameWithoutExt() {
        return com.google.common.io.Files.getNameWithoutExtension(getFullName());
    }

    @Override
    public String getExtension() {
        return com.google.common.io.Files.getFileExtension(getFullName());
    }

    @Override
    public Long getLength() {
        return fileLength;
    }

}
