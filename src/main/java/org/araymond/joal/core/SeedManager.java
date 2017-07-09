package org.araymond.joal.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.client.emulated.BitTorrentClientProvider;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.events.global.SeedSessionHasEndedEvent;
import org.araymond.joal.core.events.global.SeedSessionHasStartedEvent;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorent.client.Client;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by raymo on 27/01/2017.
 */
@Component
public class SeedManager {

    private static final Logger logger = LoggerFactory.getLogger(SeedManager.class);

    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final BitTorrentClientProvider bitTorrentClientProvider;
    private final ApplicationEventPublisher publisher;

    private ConnectionHandler connectionHandler;
    private Client currentClient;

    @PostConstruct
    private void init() throws IOException {
        connectionHandler = new ConnectionHandler(InetAddress.getLocalHost());
    }

    @PreDestroy
    private void tearDown() throws IOException {
        connectionHandler.close();
    }

    @Inject
    public SeedManager(@Value("${joal-conf}") final String joalConfFolder, final ObjectMapper mapper, final ApplicationEventPublisher publisher) throws IOException {
        this.torrentFileProvider = new TorrentFileProvider(joalConfFolder);
        this.configProvider = new JoalConfigProvider(mapper, joalConfFolder, publisher);
        this.bitTorrentClientProvider = new BitTorrentClientProvider(configProvider, mapper, joalConfFolder, publisher);
        this.publisher = publisher;
    }

    public void startSeeding() throws IOException {
        this.configProvider.init();
        this.torrentFileProvider.start();
        this.bitTorrentClientProvider.init();

        this.bitTorrentClientProvider.generateNewClient();
        final BitTorrentClient bitTorrentClient = bitTorrentClientProvider.get();

        final String id = bitTorrentClient.getPeerId();
        final Peer peer = new Peer(
                this.connectionHandler.getSocketAddress().getAddress().getHostAddress(),
                this.connectionHandler.getSocketAddress().getPort(),
                ByteBuffer.wrap(id.getBytes(Torrent.BYTE_ENCODING))
        );
        this.currentClient = new Client(
                peer,
                configProvider,
                torrentFileProvider,
                publisher,
                bitTorrentClient
        );

        this.currentClient.share();

        publisher.publishEvent(new SeedSessionHasStartedEvent(bitTorrentClient));
    }

    public void saveNewConfiguration(final AppConfiguration config) {
        this.configProvider.saveNewConf(config);
    }

    public void stop() {
        torrentFileProvider.stop();
        if (currentClient != null) {
            this.currentClient.stop();
            this.publisher.publishEvent(new SeedSessionHasEndedEvent());
        }
    }

}
