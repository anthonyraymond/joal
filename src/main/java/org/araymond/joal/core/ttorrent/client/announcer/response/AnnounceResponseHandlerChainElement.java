package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

public interface AnnounceResponseHandlerChainElement {
    void onAnnounceStartSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceStartFails(final RequestEvent event, final Announcer announcer, final Throwable throwable);
    void onAnnounceRegularSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceRegularFails(final RequestEvent event, final Announcer announcer, final Throwable throwable);
    void onAnnounceStopSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceStopFails(final RequestEvent event, final Announcer announcer, final Throwable throwable);
}
