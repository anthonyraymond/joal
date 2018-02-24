package org.araymond.joal.core;

import org.araymond.joal.core.events.global.state.GlobalSeedStartedEvent;
import org.araymond.joal.core.events.global.state.GlobalSeedStoppedEvent;
import org.araymond.joal.core.events.torrent.files.TorrentFileAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public CoreEventListener() {
    }


    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleTorrentFileAddedForSeed(final TorrentFileAddedEvent event) throws IOException {
        logger.debug("Event TorrentFileAddedEvent caught.");
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasStarted(final GlobalSeedStartedEvent event) {
        logger.debug("Event GlobalSeedStartedEvent caught.");
        // TODO : add a log to tell which BitTorrent client.
        // TODO : detailed BitTorrent client log at debug log level
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasEnded(final GlobalSeedStoppedEvent event) {
        logger.debug("Event GlobalSeedStoppedEvent caught.");
        // TODO : log that the seed session is over
    }

}
