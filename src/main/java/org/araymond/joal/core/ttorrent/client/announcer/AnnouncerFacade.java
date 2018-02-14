package org.araymond.joal.core.ttorrent.client.announcer;

import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AnnouncerFacade {
    int getLastKnownInterval();
    int getConsecutiveFails();
    Optional<Integer> getLastKnownLeechers();
    Optional<Integer> getLastKnownSeeders();
    Optional<LocalDateTime> getLastAnnouncedAt();
    String getTorrentName();
    InfoHash getTorrentInfoHash();
}
