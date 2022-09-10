package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

import java.util.ArrayList;
import java.util.List;

// TODO: replace this custom logic with a message bus
@Slf4j
public class AnnounceResponseHandlerChain implements AnnounceResponseCallback {
    private final List<AnnounceResponseHandler> handlers = new ArrayList<>();

    public void appendHandler(final AnnounceResponseHandler element) {
        this.handlers.add(element);
    }

    @Override
    public void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer) {
        handlers.forEach(e -> e.onAnnouncerWillAnnounce(announcer, event));
    }

    @Override
    public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
        handlers.forEach(handler -> {
            switch (event) {
                case STARTED: {
                    handler.onAnnounceStartSuccess(announcer, result);
                    break;
                }
                case NONE: {
                    handler.onAnnounceRegularSuccess(announcer, result);
                    break;
                }
                case STOPPED: {
                    handler.onAnnounceStopSuccess(announcer, result);
                    break;
                }
                default: {
                    log.warn("Event [{}] cannot be handled by {}", event.getEventName(), getClass().getSimpleName());
                    break;
                }
            }
        });
    }

    @Override
    public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {
        handlers.forEach(handler -> {
            switch (event) {
                case STARTED: {
                    handler.onAnnounceStartFails(announcer, throwable);
                    break;
                }
                case NONE: {
                    handler.onAnnounceRegularFails(announcer, throwable);
                    break;
                }
                case STOPPED: {
                    handler.onAnnounceStopFails(announcer, throwable);
                    break;
                }
                default: {
                    log.warn("Event [{}] cannot be handled by {}", event.getEventName(), getClass().getSimpleName());
                    break;
                }
            }
        });
    }

    @Override
    public void onTooManyAnnounceFailedInARow(final RequestEvent event, final Announcer announcer, final TooManyAnnouncesFailedInARowException e) {
        handlers.forEach(ce -> ce.onTooManyAnnounceFailedInARow(announcer, e));
    }
}
