package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

public interface AnnounceResponseHandler {
    void onAnnouncerWillAnnounce(Announcer announcer, RequestEvent event);
    void onAnnounceStartSuccess(Announcer announcer, SuccessAnnounceResponse result);
    void onAnnounceStartFails(Announcer announcer, Throwable throwable);
    void onAnnounceRegularSuccess(Announcer announcer, SuccessAnnounceResponse result);
    void onAnnounceRegularFails(Announcer announcer, Throwable throwable);
    void onAnnounceStopSuccess(Announcer announcer, SuccessAnnounceResponse result);
    void onAnnounceStopFails(Announcer announcer, Throwable throwable);
    void onTooManyAnnounceFailedInARow(Announcer announcer, TooManyAnnouncesFailedInARowException e);
}
