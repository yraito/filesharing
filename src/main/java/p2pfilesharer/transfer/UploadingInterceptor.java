package p2pfilesharer.transfer;

import p2pfilesharer.transfer.impl.ResponsePacket;
import p2pfilesharer.transfer.impl.RequestPacket;
import p2pfilesharer.transfer.InterceptorChain;
import p2pfilesharer.transfer.impl.StreamManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import p2pfilesharer.publish.impl.ShareInfo;
import p2pfilesharer.publish.impl.ShareInfo.SharePolicy;
import p2pfilesharer.common.SeekableInputStream;
import p2pfilesharer.transfer.impl.ResponsePacket.Status;

/**
 *
 * @author Nick
 */
public class UploadingInterceptor implements Interceptor {

    private final static Logger logger = LoggerFactory.getLogger(UploadingInterceptor.class);
    
    private class PathAndInfo {
        
        final ShareInfo shareInfo;
        final String filePath;

        PathAndInfo(ShareInfo shareInfo, String filePath) {
            this.shareInfo = shareInfo;
            this.filePath = filePath;
        }
    }
    
    private class PeerAndFile {
        final Number160 peerId;
        final Number160 fileId;

        PeerAndFile(Number160 peerId, Number160 fileId) {
            this.peerId = peerId;
            this.fileId = fileId;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + Objects.hashCode(this.peerId);
            hash = 79 * hash + Objects.hashCode(this.fileId);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PeerAndFile other = (PeerAndFile) obj;
            if (!Objects.equals(this.peerId, other.peerId)) {
                return false;
            }
            if (!Objects.equals(this.fileId, other.fileId)) {
                return false;
            }
            return true;
        } 
    }
    
    StreamManager streamManager;
    LocalFileFactory fileFactory;
    Cache<Number160, PathAndInfo> shareCache = CacheBuilder.newBuilder().build();
    Cache<PeerAndFile, SeekableInputStream> inputStreams;

    public UploadingInterceptor(StreamManager streamManager, LocalFileFactory fileFactory) {
        this.streamManager = streamManager;
        this.fileFactory = fileFactory;
        
    }

    public void share(ShareInfo shareInfo, String filePath) {
        shareCache.put(shareInfo.remoteFile.getId(), new PathAndInfo(shareInfo, filePath));
    }
    
    public void unshare(Number160 fileId) {
        shareCache.invalidate(fileId);
    }
    
    @Override
    public Object reply(Object m, PeerDHT thisPeer, PeerAddress thatPeer, InterceptorChain chain) {
        if (!(m instanceof RequestPacket)) {
            return chain.nextInterceptor(thisPeer, thatPeer, m);
        }

        logger.info("Handling transfer request: {}", m);
        try {
            RequestPacket reqPacket = (RequestPacket) m;
            Number160 fileId = reqPacket.getFileId();
            PathAndInfo pathInfo = shareCache.getIfPresent(fileId);
            if (pathInfo == null) {
                return ResponsePacket.failure(Status.NOT_FOUND, null, "No shared file with given id");
            }
           
            LocalFile localFile = fileFactory.getResource(pathInfo.filePath);
            if (localFile == null) {
                return ResponsePacket.failure(Status.NOT_FOUND, null, "Shared file with given id supposed to exist, but couldn't find file");
            }
            if (!pathInfo.shareInfo.sharePolicy.isAllowedToDownload(fileId, localFile)) {
                return ResponsePacket.failure(Status.NO_PERMISSION, null, "File with given id found, but you don't have permission");
            }
            Range<Long> haveRange = Range.closedOpen(0L, localFile.getLength());
            Range<Long> wantRange = reqPacket.getWantedRange();
            RangeSet<Long> haveRanges = TreeRangeSet.create();
            haveRanges.add(haveRange);
            if (!haveRange.encloses(wantRange)) {
                return ResponsePacket.failure(Status.MISSING_SOME, haveRanges);
            }
            switch (reqPacket.getMethod()) {
                case WANT:
                    return ResponsePacket.success(haveRanges);
                case GET:
                    PeerAndFile peerFile = new PeerAndFile(thatPeer.peerId(), pathInfo.shareInfo.remoteFile.getId());
                    SeekableInputStream fileIn = inputStreams.getIfPresent(peerFile);
                    if (fileIn == null) {
                        throw new RuntimeException("No InputStream returned by cache loader");
                    }
                    
                    long startPos = wantRange.lowerEndpoint();
                    long nBytes = wantRange.upperEndpoint() - startPos;
                    fileIn.seek(startPos);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream((int) nBytes);
                    try (OutputStream os = streamManager.createThrottledUpload(baos)) {
                        ByteStreams.copy(fileIn, os);
                    }
                    return ResponsePacket.success(wantRange, baos.toByteArray());
                default:
                    throw new IOException("Unknown method");
            }
            //want, unwant, get
        } catch(IOException | RuntimeException e) {
            e.printStackTrace();
            logger.error("Exception handling request {} : {}", m, e);
            return ResponsePacket.failure(e);
        } 
    }

}
