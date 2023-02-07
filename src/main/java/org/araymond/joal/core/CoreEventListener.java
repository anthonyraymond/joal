package org.araymond.joal.core;

import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.events.global.state.GlobalSeedStartedEvent;
import org.araymond.joal.core.events.global.state.GlobalSeedStoppedEvent;
import org.araymond.joal.core.events.torrent.files.TorrentFileAddedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Intercept core event, method can be @Async. They MUST NOT interact with JOAL state, otherwise this class
 * will soon turn into a god damn mess and we won't be able to maintain the code because of all the non explicit method calls.
 */
@Component
@Slf4j
public class CoreEventListener {
    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    public void handleTorrentFileAddedForSeed(final TorrentFileAddedEvent event) {
        log.debug("Event TorrentFileAddedEvent caught");
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasStarted(final GlobalSeedStartedEvent event) {
        log.debug("Event GlobalSeedStartedEvent caught");
        // TODO : add a log to tell which BitTorrent client.
        // TODO : detailed BitTorrent client log at debug log level
    }

    @Async
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    public void handleSeedSessionHasEnded(final GlobalSeedStoppedEvent event) {
        log.debug("Event GlobalSeedStoppedEvent caught");
        // TODO : log that the seed session is over
    }

}
