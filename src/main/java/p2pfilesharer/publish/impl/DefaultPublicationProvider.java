package p2pfilesharer.publish.impl;


import p2pfilesharer.publish.Publisher;
import p2pfilesharer.publish.Searcher;
import p2pfilesharer.publish.Subscriber;
import p2pfilesharer.publish.PublicationProvider;
import p2pfilesharer.publish.impl.publish.DefaultPublisher;
import p2pfilesharer.publish.impl.search.DefaultSearcher;
import p2pfilesharer.publish.impl.subscribe.PollingSubscriber;

/**
 *
 * @author Nick
 */
public class DefaultPublicationProvider<K> implements PublicationProvider<K> {

    private final Publisher<K> publisher;
    private final Subscriber<K> subscriber;
    private final Searcher<K> searcher;

    public DefaultPublicationProvider(Publisher<K> publisher, Subscriber<K> subscriber, Searcher<K> searcher) {
        this.publisher = publisher;
        this.subscriber = subscriber;
        this.searcher = searcher;
    }
    
    public DefaultPublicationProvider(Class<K> clazz, DhtEntryGenerator<K> dhtEntryGenerator) {
        this.publisher = new DefaultPublisher(dhtEntryGenerator);
        this.searcher = new DefaultSearcher(dhtEntryGenerator);
        this.subscriber = new PollingSubscriber(clazz, searcher, dhtEntryGenerator);
    }

    @Override
    public Publisher<K> publisher() {
        return publisher;
    }

    @Override
    public Subscriber<K> subscriber() {
        return subscriber;
    }

    @Override
    public Searcher<K> searcher() {
        return searcher;
    }
 
}
