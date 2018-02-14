package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

public interface AnnounceResponseCallback {
    void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer);
    void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result);
    void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable);
    void onTooManyAnnounceFailedInARaw(final RequestEvent event, final Announcer announcer, final TooMuchAnnouncesFailedInARawException e);
}
