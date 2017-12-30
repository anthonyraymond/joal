package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

public interface AnnounceResponseCallback {
    void onAnnounceSuccess(final TrackerMessage.AnnounceRequestMessage.RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceFailure(final TrackerMessage.AnnounceRequestMessage.RequestEvent event, final Announcer announcer, final Throwable throwable);
}
