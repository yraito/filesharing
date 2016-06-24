package p2pfilesharer.publish;


/**
 *
 * @author Nick
 */
public interface PublicationProvider<K> {
    
    Publisher<K> publisher();
    
    Subscriber<K> subscriber();
 
    Searcher<K> searcher();
}
