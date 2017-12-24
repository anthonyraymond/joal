package org.araymond.joal.core.ttorrent.client.announcer.thread.factory;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.announcer.SuccessAnnounceResponse;

public interface AnnounceThreadCallback {
    void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable);
}
