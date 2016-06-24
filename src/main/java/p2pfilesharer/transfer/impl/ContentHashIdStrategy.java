
package p2pfilesharer.transfer.impl;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Nick
 */
public class ContentHashIdStrategy implements IdStrategy {

    private final static Logger logger = LoggerFactory.getLogger(ContentHashIdStrategy.class);

    @Override
    public Number160 generateId(Path filePath, PeerDHT thisPeer) throws IOException {
        
        logger.info("Computing file id as SHA-1 hash for {}", filePath);
        try {

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            try (InputStream fileIn = Files.newInputStream(filePath, StandardOpenOption.READ);
                    DigestInputStream digestIn = new DigestInputStream(fileIn, sha1)) {
                byte[] buf = new byte[8192];
                ByteStreams.readFully(digestIn, buf);
                byte[] digest = sha1.digest();
                Number160 fileId = Number160.createHash(new String(digest, "UTF-8"));
                logger.info("SHA-1 digest: {}; Number160: {}", new String(digest), fileId);
                return fileId;
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Error obtaining SHA-1 MessageDigest", ex);
        }
    }

}
