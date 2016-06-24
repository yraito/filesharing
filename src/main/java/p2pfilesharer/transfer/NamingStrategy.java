package p2pfilesharer.transfer;

import java.nio.file.Path;
import java.util.List;


/**
 * Strategy for choosing destination filenames for downloaded resources.
 * 
 * @author Nick
 */
public interface NamingStrategy {


    List<Path> getFilePaths(List<RemoteFile> remoteFiles, Path outputDir);
}
