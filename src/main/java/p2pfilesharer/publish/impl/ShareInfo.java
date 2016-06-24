package p2pfilesharer.publish.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import net.tomp2p.peers.Number160;
import p2pfilesharer.transfer.File;
import p2pfilesharer.transfer.RemoteFile;

/**
 *
 * @author Nick
 */
public class ShareInfo {

    public interface SharePolicy {

        String getDescription();
        boolean isAllowedToDownload(Number160 peerId, File localFile);
    }

    public static class AllowAllSharePolicy implements SharePolicy {
        
        @Override
        public String getDescription() {
            return "Allow: All";
        }
        @Override
        public boolean isAllowedToDownload(Number160 peerId, File localFile) {
            return true;
        }
        
    }

    public static class AllowOneSharePolicy implements SharePolicy {

        Number160 sharedWith;

        public AllowOneSharePolicy(Number160 sharedWith) {
            this.sharedWith = sharedWith;
        }
        
        @Override
        public String getDescription() {
            return "Allow: " + sharedWith;
        }

        @Override
        public boolean isAllowedToDownload(Number160 peerId, File localFile) {
            return sharedWith.equals(peerId);
        } 
    }

    //Number160 fileId;
    //String filePath;
    public final RemoteFile remoteFile;
    public final SharePolicy sharePolicy;
    
    public final Number160 indexFileId;
    public final Number160 indexSharerId;
    public final String indexFileName;
    public final String indexFileExtension;
    public final Set<String> indexKeywords;
    

    public ShareInfo(RemoteFile remoteFile, SharePolicy sharePolicy) throws IOException {
        this.remoteFile = remoteFile;
        this.sharePolicy = sharePolicy;
        this.indexSharerId = remoteFile.getLocation(true).peerId();
        this.indexFileId = remoteFile.getId();
        this.indexFileName = remoteFile.getFullName();
        this.indexFileExtension = remoteFile.getExtension();
        String[] kws = remoteFile.getNameWithoutExt().toLowerCase().split(" ");
        this.indexKeywords = new TreeSet<>(Arrays.asList(kws));
    }
    
    

}
