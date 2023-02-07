package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.ttorrent.client.DelayQueue;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

import java.time.temporal.ChronoUnit;

import static org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest.*;

@RequiredArgsConstructor
@Slf4j
public class AnnounceReEnqueuer implements AnnounceResponseHandler {
    private final DelayQueue<AnnounceRequest> delayQueue;

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
        // noop
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Enqueue torrent {} in regular queue", announcer.getTorrentInfoHash().getHumanReadable());
        this.delayQueue.addOrReplace(createRegular(announcer), result.getInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
        log.debug("Enqueue torrent {} in start queue once again (because it failed)", announcer.getTorrentInfoHash().getHumanReadable());
        this.delayQueue.addOrReplace(createStart(announcer), announcer.getLastKnownInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Enqueue torrent {} in regular queue", announcer.getTorrentInfoHash().getHumanReadable());
        this.delayQueue.addOrReplace(createRegular(announcer), result.getInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
        log.debug("Enqueue torrent {} in regular queue once again (because it failed)", announcer.getTorrentInfoHash().getHumanReadable());
        this.delayQueue.addOrReplace(createRegular(announcer), announcer.getLastKnownInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        // noop
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
        log.debug("Enqueue torrent {} in stop queue once again (because it failed)", announcer.getTorrentInfoHash().getHumanReadable());
        this.delayQueue.addOrReplace(createStop(announcer), 0, ChronoUnit.SECONDS);
    }

    @Override
    public void onTooManyAnnounceFailedInARow(final Announcer announcer, final TooManyAnnouncesFailedInARowException e) {
        // noop
    }
}
