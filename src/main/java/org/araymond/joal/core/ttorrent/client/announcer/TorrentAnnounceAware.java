package org.araymond.joal.core.ttorrent.client.announcer;

public interface TorrentAnnounceAware {

    void successfullyStarted(final Announcer announcer, final SuccessAnnounceResponse successAnnounceResponse);
    void successfullyAnnounced(final Announcer announcer, final SuccessAnnounceResponse successAnnounceResponse);
    void announcedStop(final Announcer announcer);
    void failedToAnnounce(final Announcer announcer, final String err);
    void tooManyFailedAnnouncedInARaw(final Announcer announcer, final int consecutiveFails);

}
