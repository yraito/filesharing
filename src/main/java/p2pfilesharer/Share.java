
package p2pfilesharer;

import p2pfilesharer.transfer.File;
import p2pfilesharer.transfer.UploadingInterceptor;
import com.google.common.util.concurrent.ListenableFuture;
import java.nio.file.Path;
import net.tomp2p.p2p.Shutdown;
import p2pfilesharer.publish.Publisher;
import p2pfilesharer.publish.impl.ShareInfo;
import p2pfilesharer.publish.impl.ShareInfo.SharePolicy;
import p2pfilesharer.common.*;
/**
 *
 * @author Nick
 */
public class Share {
    
    ShareInfo shareInfo;
    Publisher<ShareInfo> publisher;
    UploadingInterceptor uploader;
    Path filePath;
    Shutdown shutdown;

    public Share(ShareInfo shareInfo, UploadingInterceptor uploader, Path filePath, Shutdown shutdown) {
        this.shareInfo = shareInfo;
        this.uploader = uploader;
        this.filePath = filePath;
        this.shutdown = shutdown;
    }
    
    
    public File getFile() {
        return shareInfo.remoteFile;
    }
    
    public Path getFilePath() {
        return filePath;
    }
    
    public SharePolicy getPolicy() {
        return shareInfo.sharePolicy;
    }
    
    public ListenableFuture<Void> cancel() {
        return Util.toFuture(shutdown.shutdown());
    }
    //Future cancel
    //getFiles(Local files)
    //getShares
    //getPolicy
    //isSharedWIth(...)
    //state
    //shutdown
    //    Set<K> getSharedResources();
    //boolean isShared(Object r, Object peer);
}
