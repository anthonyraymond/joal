package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

public interface AnnounceResponseCallback {
    void onAnnounceWillAnnounce(RequestEvent event, Announcer announcer);
    void onAnnounceSuccess(RequestEvent event, Announcer announcer, SuccessAnnounceResponse result);
    void onAnnounceFailure(RequestEvent event, Announcer announcer, Throwable throwable);
    void onTooManyAnnounceFailedInARow(RequestEvent event, Announcer announcer, TooManyAnnouncesFailedInARowException e);
}
