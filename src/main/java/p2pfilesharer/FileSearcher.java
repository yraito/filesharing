
package p2pfilesharer;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import p2pfilesharer.publish.Searcher;
import p2pfilesharer.publish.Where;
import p2pfilesharer.publish.impl.ShareInfo;
import p2pfilesharer.publish.impl.search.AbstractWhere;
import p2pfilesharer.transfer.RemoteFile;

/**
 *
 * @author Nick
 */
public class FileSearcher {

    private final Searcher<ShareInfo> searcher;

    public FileSearcher(Searcher<ShareInfo> searcher) {
        this.searcher = searcher;
    }

    public Where<RemoteFile> whereLowerFilenameEquals(String fileName) {
        try {
            Field nameField = ShareInfo.class.getDeclaredField("indexFileName");
            Where<ShareInfo> where1 = searcher.whereLowerEquals(nameField, fileName);
            return new WhereAdapter(where1);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Where<RemoteFile> whereLowerFilenameContains(Set<String> keywords) {
        try {
            Field field = ShareInfo.class.getDeclaredField("indexKeywords");
            Where<ShareInfo> where1 = searcher.whereLowerContainsKeywords(field, keywords);
            return new WhereAdapter(where1);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Where<RemoteFile> whereLowerExtensionEquals(String fileExtension) {
        try {
            Field field = ShareInfo.class.getDeclaredField("indexFileExtension");
            Where<ShareInfo> where1 = searcher.whereLowerEquals(field, fileExtension);
            return new WhereAdapter(where1);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Where<RemoteFile> whereSharedBy(Number160 sharerId) {
        try {
            Field field = ShareInfo.class.getDeclaredField("indexSharerId");
            Where<ShareInfo> where1 = searcher.whereEquals(field, sharerId);
            return new WhereAdapter(where1);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class WhereAdapter extends AbstractWhere<RemoteFile> {

        Where<ShareInfo> where;

        WhereAdapter(Where<ShareInfo> where) {
            this.where = where;
        }

        @Override
        public ListenableFuture<Set<RemoteFile>> lookup(PeerDHT thisPeer, int limit) {
            return Futures.transform(where.lookup(thisPeer, limit),
                    (Set<ShareInfo> s)
                    -> (Set<RemoteFile>) s.stream()
                            .map(t -> t.remoteFile)
                            .collect(Collectors.toSet())
            );
        }
    }
}
