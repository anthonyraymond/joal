package org.araymond.joal.web.messages.outgoing.impl.announce;

import lombok.Getter;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.time.LocalDateTime;

@Getter
public abstract class AnnouncePayload implements MessagePayload {
    private final InfoHash infoHash;
    private final String torrentName;
    private final long torrentSize;
    private final int lastKnownInterval;
    private final int consecutiveFails;
    private final LocalDateTime lastAnnouncedAt;
    private final Integer lastKnownLeechers;
    private final Integer lastKnownSeeders;

    protected AnnouncePayload(final AnnouncerFacade announcerFacade) {
        this.infoHash = announcerFacade.getTorrentInfoHash();
        this.torrentName = announcerFacade.getTorrentName();
        this.torrentSize = announcerFacade.getTorrentSize();
        this.lastKnownInterval = announcerFacade.getLastKnownInterval();
        this.consecutiveFails = announcerFacade.getConsecutiveFails();
        this.lastAnnouncedAt = announcerFacade.getLastAnnouncedAt().orElse(null);
        this.lastKnownLeechers = announcerFacade.getLastKnownLeechers().orElse(null);
        this.lastKnownSeeders = announcerFacade.getLastKnownSeeders().orElse(null);
    }
}
