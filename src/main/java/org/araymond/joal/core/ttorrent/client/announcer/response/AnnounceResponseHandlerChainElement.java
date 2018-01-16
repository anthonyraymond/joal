package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

public interface AnnounceResponseHandlerChainElement {
    void onAnnouncerWillAnnounce(final Announcer announcer, RequestEvent event);
    void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceStartFails(final Announcer announcer, final Throwable throwable);
    void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable);
    void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceStopFails(final Announcer announcer, final Throwable throwable);
    void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e);
}
