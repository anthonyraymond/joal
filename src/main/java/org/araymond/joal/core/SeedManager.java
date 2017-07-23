package org.araymond.joal.core;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public void init() throws IOException {
        this.connectionHandler.init();
        this.torrentFileProvider.start();
    }

    public void tearDown() throws IOException {
        this.connectionHandler.close();
        this.currentClient.stop();
    }

    @Inject
    public SeedManager(@Value("${joal-conf}") final String joalConfFolder, final ObjectMapper mapper, final ApplicationEventPublisher publisher) throws IOException {
        this.torrentFileProvider = new TorrentFileProvider(joalConfFolder, publisher);
        this.configProvider = new JoalConfigProvider(mapper, joalConfFolder, publisher);
        this.bitTorrentClientProvider = new BitTorrentClientProvider(configProvider, mapper, joalConfFolder, publisher);
        this.publisher = publisher;
        this.connectionHandler = new ConnectionHandler();
    }

    public void startSeeding() throws IOException {
        this.configProvider.init();
        this.bitTorrentClientProvider.init();
        this.bitTorrentClientProvider.generateNewClient();
        final BitTorrentClient bitTorrentClient = bitTorrentClientProvider.get();

        this.currentClient = new Client(
                this.connectionHandler,
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

    public void saveTorrentToDisk(final String name, final byte[] bytes) {
        this.torrentFileProvider.saveTorrentFileToDisk(name, bytes);
    }

    public void stop() {
        if (currentClient != null) {
            this.currentClient.stop();
            this.publisher.publishEvent(new SeedSessionHasEndedEvent());
        }
    }

}
