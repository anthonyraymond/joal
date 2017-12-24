package org.araymond.joal.core.ttorrent.client.announcer;

import com.google.common.util.concurrent.FutureCallback;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.announcer.SuccessAnnounceResponse;
import org.araymond.joal.core.ttorrent.client.announcer.thread.factory.AnnounceThreadCallback;
import org.araymond.joal.core.ttorrent.client.announcer.thread.factory.AnnounceThreadFactory;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class AnnouncersOrchestrator implements AnnounceThreadCallback {
    private static final Logger logger = getLogger(AnnouncersOrchestrator.class);
    private final AnnouncerResolver announcerResolver;
    private final AnnounceThreadFactory announceThreadFactory;
    private volatile boolean isStopState = false;
    // TODO: a list of todo action (like Announcer => Start,), a kind of Command pattern can be good here
    // TODO: when a stop is requested: announcerResolver.getForInfohash(infohash) and enqueue this as a stop event
    // TODO: Run events into a ThreadPoolExecutor (nice methods avaiable like getCount).
    // TODO: the executor must be notified that there is something to do. (this class polls the queue every X seconds and notify the executor if needed)

    public AnnouncersOrchestrator(final AnnounceThreadFactory announceThreadFactory) {
        this.announceThreadFactory = announceThreadFactory;
        this.announcerResolver = new AnnouncerResolver();
    }


    @Override
    public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
        switch (event) {
            case STARTED:
                // TODO: append to queue with interval
                break;
            case NONE:
                // TODO: append to queue with interval
                break;
            case STOPPED:
                // TODO: request new one

                break;
            default:
                logger.warn("AnnouncersOrchestrator had to handle {} event, but didn't knew how to. Did nothing with announcer's callback for torrent {}.", event.name(), announcer.getTorrent().getName());
        }
    }

    @Override
    public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {

    }


}
