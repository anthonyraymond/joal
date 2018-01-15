package org.araymond.joal.core.ttorrent.client;

import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnouncerExecutor;
import org.araymond.joal.core.ttorrent.client.announcer.response.*;
import org.springframework.context.ApplicationEventPublisher;

public final class ClientBuilder {
    private JoalConfigProvider configProvider;
    private TorrentFileProvider torrentFileProvider;
    private BandwidthDispatcher bandwidthDispatcher;
    private AnnounceDataAccessor announceDataAccessor;
    private ApplicationEventPublisher eventPublisher;

    private ClientBuilder() {
    }
    
    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public ClientBuilder withConfigProvider(final JoalConfigProvider configProvider) {
        this.configProvider = configProvider;
        return this;
    }

    public ClientBuilder withTorrentFileProvider(final TorrentFileProvider torrentFileProvider) {
        this.torrentFileProvider = torrentFileProvider;
        return this;
    }

    public ClientBuilder withBandwidthDispatcher(final BandwidthDispatcher bandwidthDispatcher) {
        this.bandwidthDispatcher = bandwidthDispatcher;
        return this;
    }

    public ClientBuilder withAnnounceDataAccessor(final AnnounceDataAccessor announceDataAccessor) {
        this.announceDataAccessor = announceDataAccessor;
        return this;
    }

    public ClientBuilder withEventPublisher(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        return this;
    }

    public ClientFacade build() {
        final AnnouncerExecutor announcerExecutor = new AnnouncerExecutor();
        final DelayQueue<AnnounceRequest> delayQueue = new DelayQueue<>();

        final AnnounceResponseHandlerChain announceResponseCallback = new AnnounceResponseHandlerChain();
        announceResponseCallback.appendHandler(new AnnounceEventPublisher(this.eventPublisher));
        announceResponseCallback.appendHandler(new AnnounceReEnqueuer(delayQueue));
        announceResponseCallback.appendHandler(new BandwidthDispatcherNotifier(bandwidthDispatcher));
        final ClientNotifier clientNotifier = new ClientNotifier();
        announceResponseCallback.appendHandler(clientNotifier);

        final Client client = new Client(this.configProvider, this.torrentFileProvider, announcerExecutor, delayQueue, announceResponseCallback, this.announceDataAccessor);
        clientNotifier.setClient(client);

        return client;
    }

}
