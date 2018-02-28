package org.araymond.joal.core.ttorrent.client;

import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFactory;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnouncerExecutor;
import org.araymond.joal.core.ttorrent.client.announcer.response.*;
import org.springframework.context.ApplicationEventPublisher;

public final class ClientBuilder {
    private AppConfiguration appConfiguration;
    private TorrentFileProvider torrentFileProvider;
    private BandwidthDispatcher bandwidthDispatcher;
    private AnnouncerFactory announcerFactory;
    private ApplicationEventPublisher eventPublisher;
    private DelayQueue<AnnounceRequest> delayQueue;

    private ClientBuilder() {
    }
    
    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public ClientBuilder withAppConfiguration(final AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
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

    public ClientBuilder withAnnouncerFactory(final AnnouncerFactory announcerFactory) {
        this.announcerFactory = announcerFactory;
        return this;
    }

    public ClientBuilder withEventPublisher(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        return this;
    }

    public ClientBuilder withDelayQueue(final DelayQueue<AnnounceRequest> delayQueue) {
        this.delayQueue = delayQueue;
        return this;
    }

    public ClientFacade build() {
        final AnnounceResponseHandlerChain announceResponseCallback = new AnnounceResponseHandlerChain();
        announceResponseCallback.appendHandler(new AnnounceEventPublisher(this.eventPublisher));
        announceResponseCallback.appendHandler(new AnnounceReEnqueuer(this.delayQueue));
        announceResponseCallback.appendHandler(new BandwidthDispatcherNotifier(bandwidthDispatcher));
        final ClientNotifier clientNotifier = new ClientNotifier();
        announceResponseCallback.appendHandler(clientNotifier);

        final AnnouncerExecutor announcerExecutor = new AnnouncerExecutor(announceResponseCallback);

        final Client client = new Client(this.appConfiguration, this.torrentFileProvider, announcerExecutor, this.delayQueue, this.announcerFactory);
        clientNotifier.setClient(client);

        return client;
    }

}
