/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import p2pfilesharer.connect.PeerConnector;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.p2p.Shutdown;
import net.tomp2p.peers.PeerAddress;
import p2pfilesharer.publish.PublicationProvider;
import p2pfilesharer.publish.impl.DefaultPublicationProvider;
import p2pfilesharer.publish.impl.DhtEntryGenerator;
import p2pfilesharer.publish.impl.DhtEntryGenerator.LocaterType;
import p2pfilesharer.publish.impl.PeerInfo;
import p2pfilesharer.publish.impl.ShareInfo;
import p2pfilesharer.publish.impl.ShareInfo.SharePolicy;
import p2pfilesharer.publish.impl.search.QueryResolvingInterceptor;
import p2pfilesharer.transfer.BandwidthThrottler;
import p2pfilesharer.transfer.DownloadStore;
import p2pfilesharer.transfer.Interceptor;
import p2pfilesharer.transfer.InterceptorChain;
import p2pfilesharer.transfer.LocalFile;
import p2pfilesharer.transfer.LocalFileFactory;
import p2pfilesharer.transfer.RemoteFile;
import p2pfilesharer.transfer.Transfer;
import p2pfilesharer.transfer.TransferFactory;
import p2pfilesharer.transfer.UploadingInterceptor;
import p2pfilesharer.transfer.impl.BasicRemoteFile;
import p2pfilesharer.transfer.impl.ContentHashIdStrategy;
import p2pfilesharer.transfer.impl.DefaultTransferFactory;
import p2pfilesharer.transfer.impl.FileSystemDownloadStore;
import p2pfilesharer.transfer.impl.FileSystemLocalFileFactory;
import p2pfilesharer.transfer.impl.StreamManager;
import p2pfilesharer.common.*;

/**
 *
 * @author Nick
 */
public class FilePeer {

    private final AtomicReference<PeerDHT> thisPeer = new AtomicReference<>();
    private final EventBus eventBus = new EventBus();
    private final StreamManager streamManager = new StreamManager();
    private final TransferFactory transferFactory = new DefaultTransferFactory(streamManager, eventBus);
    private final LocalFileFactory localFileFactory = new FileSystemLocalFileFactory(new ContentHashIdStrategy());
    private final UploadingInterceptor uploader = new UploadingInterceptor(streamManager, localFileFactory);
    private final QueryResolvingInterceptor resolver = new QueryResolvingInterceptor();
    private final PeerConnector bootstrapper;
    private final DownloadStore downloadStore;
    private final PublicationProvider<ShareInfo> filePublicationProvider;
    private final PublicationProvider<PeerInfo> peerPublicationProvider;
    private final List<Interceptor> interceptors = new ArrayList<>(2);

    public FilePeer(Path downloadPath, PeerConnector bootstrapper) {
        try {
            this.downloadStore = new FileSystemDownloadStore(downloadPath);
            this.bootstrapper = bootstrapper;
            Class<ShareInfo> clazz = ShareInfo.class;
            DhtEntryGenerator<ShareInfo> shareEntryGen = new DhtEntryGenerator<>(ShareInfo.class, ShareInfo.class.getDeclaredField("indexFileId"));
            shareEntryGen.addIndex(ShareInfo.class.getDeclaredField("indexFileName"), LocaterType.EQUALS_IGNORE_CASE);
            shareEntryGen.addIndex(ShareInfo.class.getDeclaredField("indexFileExtension"), LocaterType.EQUALS_IGNORE_CASE);
            shareEntryGen.addIndex(ShareInfo.class.getDeclaredField("indexKeywords"), LocaterType.CONTAINS_KEYWORDS);
            shareEntryGen.addIndex(ShareInfo.class.getDeclaredField("indexSharerId"), LocaterType.EQUALS);
            this.filePublicationProvider = new DefaultPublicationProvider(clazz, shareEntryGen);
            Class<PeerInfo> clazz2 = PeerInfo.class;
            DhtEntryGenerator<PeerInfo> peerEntryGen = new DhtEntryGenerator<>(PeerInfo.class, PeerInfo.class.getDeclaredField("indexPeerId"));
            peerEntryGen.addIndex(ShareInfo.class.getDeclaredField("LOCATION_KEY"), LocaterType.EQUALS);
            this.peerPublicationProvider = new DefaultPublicationProvider(clazz2, peerEntryGen);
            this.interceptors.add(resolver);
            this.interceptors.add(uploader);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public ListenableFuture<FilePeer> connect(String alias) {
        Function<PeerDHT, FilePeer> func = (PeerDHT p) -> {
            p.peer().objectDataReply((PeerAddress pa, Object o) -> {
                InterceptorChain ic = new InterceptorChain(interceptors);
                return ic.nextInterceptor(thisPeer.get(), pa, o);
            });
            try {
                PeerInfo peerInfo = new PeerInfo(alias, p.peerAddress());
                peerPublicationProvider.publisher().publish(p, peerInfo, null);
                peerPublicationProvider.subscriber().subscribeByField(
                        PeerInfo.class.getDeclaredField("LOCATION_KEY"),
                        PeerInfo.LOCATION_KEY,
                        p,
                        eventBus
                );
                return FilePeer.this;
            } catch (NoSuchFieldException | SecurityException | IOException ex) {
                throw new RuntimeException(ex);
            } 
        };
        ListenableFuture<PeerDHT> peer = this.bootstrapper.connect(alias);
        return Futures.transform(peer, func);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public BandwidthThrottler getBandwidthThrottler() {
        return streamManager;
    }

    public FileSearcher createSearcher() {
        checkConnected();
        return new FileSearcher(filePublicationProvider.searcher());
    }

    public Transfer createDownload(RemoteFile remoteFile) throws IOException {
        checkConnected();
        return transferFactory.download(thisPeer.get(), remoteFile, downloadStore, null);
    }

    public Share createShare(Path file, SharePolicy sharePolicy) throws IOException {
        checkConnected();
        PeerAddress peerAddress = thisPeer.get().peerAddress();
        LocalFile localFile = localFileFactory.getResource(file.toString());
        RemoteFile remoteFile = new BasicRemoteFile(file, localFile, peerAddress);
        ShareInfo shareInfo = new ShareInfo(remoteFile, sharePolicy);
        uploader.share(shareInfo, file.toString());
        Shutdown shutdown = filePublicationProvider.publisher().publish(thisPeer.get(), shareInfo, null);
        return new Share(shareInfo, uploader, file, shutdown);
    }

    public ListenableFuture<Void> disconnect() {
        checkConnected();
        BaseFuture bf = thisPeer.getAndSet(null).shutdown();
        return Util.toFuture(bf);
    }

    void checkConnected() {
        if (thisPeer.get() == null) {
            throw new IllegalStateException("not connected");
        }
    }
}
