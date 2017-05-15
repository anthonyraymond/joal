package org.araymond.joal.core;

import org.araymond.joal.core.events.*;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
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
 * Created by raymo on 09/05/2017.
 */

/**
 * Intercept core event, method can be @Async. Most of all, it must not interact with JOAL state, otherwise this class
 * will soon turn into a god damn mess and we won't be able to maintain the code because of all the non explicit method calls.
 */
@Component
public class CoreEventListener {
    private static final Logger logger = LoggerFactory.getLogger(CoreEventListener.class);

    private final ApplicationContext appContext;
    private final SeedManager manager;
    private final TorrentFileProvider torrentFileProvider;

    @Inject
    public CoreEventListener(final ApplicationContext appContext, final SeedManager manager, final TorrentFileProvider torrentFileProvider) {
        this.appContext = appContext;
        this.manager = manager;
        this.torrentFileProvider = torrentFileProvider;
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleNoMoreTorrents(final NoMoreTorrentsFileAvailable event) {
        logger.debug("Event NoMoreTorrentsFileAvailable caught.");
        /*logger.warn("There is no more .torrent file, add some more to resume seed.");
        this.manager.stop();*/
    }

    // It HAVE TO BE async, this method is called from a thread which is started from the main thread.
    // If not async, it result in the subthread trying to stop himself in a synchronous way, and cause an
    // InterruptedException because it is stuck in the stop() method.
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleNoMoreLeechers(final NoMoreLeechers event) throws IOException {
        logger.debug("Event NoMoreLeechers caught.");
        //logger.warn("0 peers are currently leeching, moving torrent to archived and restarting seed.");
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleTorrentFileAddedForSeed(final TorrentFileAddedForSeed event) throws IOException {
        logger.debug("Event TorrentFileAddedForSeed caught.");
        /*if (this.torrentFileProvider.getTorrentCount() == 1) {
            logger.info("Resuming seed.");
        }
        this.manager.startSeeding();*/
    }


    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionWillStart(final SeedSessionWillStart event) {
        logger.debug("Event SeedSessionWillStart caught.");
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasStarted(final SeedSessionHasStarted event) {
        logger.debug("Event SeedSessionHasStarted caught.");
        // TODO : add a log to tell which torrent, which BitTorrent client.
        // TODO : detailed BitTorrent client log at debug log level
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasEnded(final SeedSessionHasEnded event) {
        logger.debug("Event SeedSessionHasEnded caught.");
        // TODO : log that the seed session is over
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSomethingHasFuckedUp(final SomethingHasFuckedUp event) {
        logger.error("Event SomethingHasFuckedUp caught.", event.getException());
        // Stop the application
        SpringApplication.exit(appContext, () -> 42);
    }

}
