package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.time.LocalDateTime;
import java.util.Optional;

public abstract class AnnouncePayload implements MessagePayload {
    private final InfoHash infoHash;
    private final String torrentName;
    private final int lastKnownInterval;
    private final int consecutiveFails;
    private final LocalDateTime lastAnnouncedAt;
    private final Integer lastKnownLeechers;
    private final Integer lastKnownSeeders;

    protected AnnouncePayload(final AnnouncerFacade announcerFacade) {
        this.infoHash = announcerFacade.getTorrentInfoHash();
        this.torrentName = announcerFacade.getTorrentName();
        this.lastKnownInterval = announcerFacade.getLastKnownInterval();
        this.consecutiveFails = announcerFacade.getConsecutiveFails();
        this.lastAnnouncedAt = announcerFacade.getLastAnnouncedAt().orElse(null);
        this.lastKnownLeechers = announcerFacade.getLastKnownLeechers().orElse(null);
        this.lastKnownSeeders = announcerFacade.getLastKnownSeeders().orElse(null);
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }
    public String getTorrentName() {
        return torrentName;
    }
    public int getLastKnownInterval() {
        return lastKnownInterval;
    }
    public int getConsecutiveFails() {
        return consecutiveFails;
    }
    public LocalDateTime getLastAnnouncedAt() {
        return lastAnnouncedAt;
    }
    public Integer getLastKnownLeechers() {
        return lastKnownLeechers;
    }
    public Integer getLastKnownSeeders() {
        return lastKnownSeeders;
    }
}
