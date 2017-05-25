package org.araymond.joal.core;

import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.client.emulated.BitTorrentClientProvider;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.events.seedsession.SeedSessionHasEndedEvent;
import org.araymond.joal.core.events.seedsession.SeedSessionHasStartedEvent;
import org.araymond.joal.core.events.seedsession.SeedSessionWillStartEvent;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorent.client.Client;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public SeedManager(final JoalConfigProvider configProvider, final TorrentFileProvider torrentFileProvider, final BitTorrentClientProvider bitTorrentClientProvider, final ApplicationEventPublisher publisher) throws IOException {
        this.configProvider = configProvider;
        this.torrentFileProvider = torrentFileProvider;
        this.bitTorrentClientProvider = bitTorrentClientProvider;
        this.publisher = publisher;
    }

    public void startSeeding() throws IOException {
        // Ensure current client is killed in case it was already started
        this.stop();

        this.bitTorrentClientProvider.generateNewClient();

        final BitTorrentClient bitTorrentClient = bitTorrentClientProvider.get();
        // TODO : still need to handle exception in this method to prevent crash on startup, particularly NoMoreTorrent

        publisher.publishEvent(new SeedSessionWillStartEvent());

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

    public void stop() {
        if (currentClient != null) {
            this.currentClient.stop();
            this.publisher.publishEvent(new SeedSessionHasEndedEvent());
        }
    }

}
