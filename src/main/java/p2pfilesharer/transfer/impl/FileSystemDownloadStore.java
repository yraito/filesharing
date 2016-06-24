
package p2pfilesharer.transfer.impl;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import p2pfilesharer.transfer.DaoException;
import p2pfilesharer.transfer.DownloadStore;
import p2pfilesharer.transfer.NamingStrategy;
import p2pfilesharer.transfer.RemoteFile;
import p2pfilesharer.common.FileSeekableOutputStream;
import p2pfilesharer.common.SeekableOutputStream;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Nick
 */
public class FileSystemDownloadStore implements DownloadStore {

    private static final Logger logger = LoggerFactory.getLogger(p2pfilesharer.transfer.impl.FileSystemDownloadStore.class);

    private static boolean createIfNotExists(Path p) throws IOException {
        try {
            Files.createFile(p);
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        }
    }

    private static boolean moveFromTempToDest(PathsKey pm) throws IOException {
        if (pm.tempPath.compareTo(pm.destPath) != 0) {
            logger.info("Moving file from {} to: {}", pm.tempPath, pm.destPath);
            createIfNotExists(pm.destPath);
            Files.move(pm.tempPath, pm.destPath, StandardCopyOption.REPLACE_EXISTING);
            pm.dest = true;
            return true;
        } else {
            logger.info("Temp path == Dest path. Not moving");
            return false;
        }
    }

    private static boolean moveIfComplete(PathsKey pm) throws IOException {
        if (pm.dest) {
            return false;
        }
        Long expSize = pm.res.getLength();
        Long actSize = Files.size(pm.getPath());
        if (expSize != null && expSize < actSize) {
            logger.warn("File incomplete. Expected: " + expSize + "; Actual: " + actSize);
            pm.dest = false;
            return false;
        } else {
            return moveFromTempToDest(pm);
        }
    }

    NamingStrategy tempNameStrategy;
    NamingStrategy destNameStrategy;
    Path tempDirectory;
    Path destDirectory;

    public FileSystemDownloadStore(Path destDirectory) {
        this(destDirectory, destDirectory);
    }

    public FileSystemDownloadStore(Path tempDirectory, Path destDirectory) {
        this(new DefaultNamingStrategy("part"), new DefaultNamingStrategy(), tempDirectory, destDirectory);
    }

    public FileSystemDownloadStore(NamingStrategy tempNameStrategy, NamingStrategy destNameStrategy, Path tempDirectory, Path destDirectory) {
        this.tempNameStrategy = tempNameStrategy;
        this.destNameStrategy = destNameStrategy;
        this.tempDirectory = tempDirectory;
        this.destDirectory = destDirectory;
        checkNotNull(tempDirectory);
        checkNotNull(destDirectory);
        try {
            Files.createDirectories(tempDirectory);
            Files.createDirectories(destDirectory);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        logger.info("Temp directory: {}", tempDirectory);
        logger.info("Download directory: {}", destDirectory);
    }

    @Override
    public StoreKey create(RemoteFile file) throws DaoException {
        logger.info("Creating file/dao key for file {}: {}", file.getId(), file.getFullName());
        try {
            List<RemoteFile> drs = Collections.singletonList(file);
            Path tempPath = tempNameStrategy.getFilePaths(drs, tempDirectory).get(0);
            Path destPath = destNameStrategy.getFilePaths(drs, destDirectory).get(0);
            logger.info("Temp path: {}; Dest path: {}", tempPath, destPath);
            Files.createDirectories(tempDirectory);
            Files.createDirectories(destDirectory);
            createIfNotExists(tempPath);
            //Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);
            //logger.info("Save complete. Moving to final dest: {}", destPath);
            //return moveIfComplete(dr, tempPath, destPath);
            return new PathsKey(file, tempPath, destPath, false);
        } catch (IOException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public SeekableOutputStream update(StoreKey id) throws DaoException {
        logger.debug("Creating update OutputStream for {}", id);
        try {
            PathsKey pm = PathsKey.checkedCast(id);
            Path p = pm.getPath();
            SeekableOutputStream sos = new FileSeekableOutputStream(p) {
                @Override
                public void close() throws IOException {
                    moveIfComplete(pm);
                }
            };
            logger.debug("created");
            return sos;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new DaoException(ex);
        }
    }

    @Override
    public boolean finish(StoreKey id) throws DaoException {
        logger.info("Finish store for {}", id);
        PathsKey pm = PathsKey.checkedCast(id);
        try {
            return moveFromTempToDest(pm);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public boolean delete(StoreKey id) throws DaoException {
        logger.debug("Deleting {}", id);
        PathsKey pm = PathsKey.checkedCast(id);
        boolean delTemp = false;
        boolean delDest = false;
        try {
            if (pm.tempPath != null) {
                delTemp = Files.deleteIfExists(pm.tempPath);
            }
            if (pm.destPath != null) {
                delDest = Files.deleteIfExists(pm.destPath);
            }
            return delTemp || delDest;
        } catch (IOException e) {
            throw new DaoException(e);
        }
    }

    private static class PathsKey implements StoreKey {

        static PathsKey checkedCast(Object id) throws InvalidKeyException, DaoException {
            checkArgument(id instanceof PathsKey, "Expecting PathsKey, got " + id.getClass());
            PathsKey pm = (PathsKey) id;
            pm.checkIfValid();
            return pm;
        }

        final Path tempPath;
        final Path destPath;
        final RemoteFile res;
        boolean dest;
        boolean valid = true;

        public PathsKey(RemoteFile res, Path tempPath, Path destPath, boolean dest) {
            this.res = res;
            this.tempPath = tempPath;
            this.destPath = destPath;
            this.dest = dest;
        }

        public String toString() {
            return "TEMP: " + tempPath.toString().replace("\\", "/")
                    + "DEST: " + destPath.toString().replace("\\", "/");
        }
        
        public String getDest() {
            return destPath.toString().replace("\\", "/");
        }

        public Path getPath() {
            return dest ? destPath : tempPath;
        }

        public void checkIfValid() throws DaoException {
            Path p = getPath();
            if (!valid) {
                throw new InvalidKeyException("Invalid key: " + p.toString());
            }
            if (!Files.exists(p)) {
                if (Files.exists(tempPath)) {
                    dest = false;
                } else if (Files.exists(destPath)) {
                    dest = true;
                } else {
                    throw new InvalidKeyException("Invalid key...DNE " + p.toString());
                }

            }
        }
    }
}
