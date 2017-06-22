package org.araymond.joal.core;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.io.FileUtils;
import org.araymond.joal.core.events.NoMoreLeechersEvent;
import org.araymond.joal.core.events.NoMoreTorrentsFileAvailableEvent;
import org.araymond.joal.core.events.SomethingHasFuckedUpEvent;
import org.araymond.joal.core.events.announce.AnnounceRequestingEvent;
import org.araymond.joal.core.events.filechange.TorrentFileAddedForSeedEvent;
import org.araymond.joal.core.events.seedsession.SeedSessionHasEndedEvent;
import org.araymond.joal.core.events.seedsession.SeedSessionHasStartedEvent;
import org.araymond.joal.core.events.seedsession.SeedSessionWillStartEvent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Intercept core event, method can be @Async. THey MUST NOT interact with JOAL state, otherwise this class
 * will soon turn into a god damn mess and we won't be able to maintain the code because of all the non explicit method calls.
 */
@Component
public class CoreEventListener {
    private static final Logger logger = LoggerFactory.getLogger(CoreEventListener.class);

    private final ApplicationContext appContext;

    @Inject
    public CoreEventListener(final ApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleAnnounceRequesting(final AnnounceRequestingEvent event) {
        final RequestEvent announceEvent = event.getEvent();
        final TorrentWithStats torrent = event.getTorrent();
        logger.info(
                "Announced {} for torrent {} Up={}/Down={}/Left={}",
                announceEvent == RequestEvent.NONE ? "" : announceEvent,
                torrent.getTorrent().getName(),
                FileUtils.byteCountToDisplaySize(torrent.getUploaded()),
                FileUtils.byteCountToDisplaySize(torrent.getDownloaded()),
                FileUtils.byteCountToDisplaySize(torrent.getLeft())
        );
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleNoMoreTorrents(final NoMoreTorrentsFileAvailableEvent event) {
        logger.debug("Event NoMoreTorrentsFileAvailableEvent caught.");
        // logger.warn("There is no more .torrent file, add some more to resume seed.");
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleNoMoreLeechers(final NoMoreLeechersEvent event) throws IOException {
        logger.debug("Event NoMoreLeechersEvent caught.");
        //logger.warn("0 peers are currently leeching, moving torrent to archived and restarting seed.");
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleTorrentFileAddedForSeed(final TorrentFileAddedForSeedEvent event) throws IOException {
        logger.debug("Event TorrentFileAddedForSeedEvent caught.");
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionWillStart(final SeedSessionWillStartEvent event) {
        logger.debug("Event SeedSessionWillStartEvent caught.");
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasStarted(final SeedSessionHasStartedEvent event) {
        logger.debug("Event SeedSessionHasStartedEvent caught.");
        // TODO : add a log to tell which BitTorrent client.
        // TODO : detailed BitTorrent client log at debug log level
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasEnded(final SeedSessionHasEndedEvent event) {
        logger.debug("Event SeedSessionHasEndedEvent caught.");
        // TODO : log that the seed session is over
    }

    @Async
    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleSomethingHasFuckedUp(final SomethingHasFuckedUpEvent event) {
        // We caught an unrecoverable exception in a thread, we better stop right now.
        logger.error("Event SomethingHasFuckedUpEvent caught.", event.getException());
        // Stop the application
        SpringApplication.exit(appContext, () -> 42);
    }

}
