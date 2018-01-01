package org.araymond.joal.core.ttorrent.client.announcer;

import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class AnnouncerResolver {
    private final Map<InfoHash, Announcer> map;


    AnnouncerResolver() {
        this.map = new HashMap<>();
    }

    void register(final InfoHash infoHash, final Announcer announcer) {
        this.map.put(infoHash, announcer);
    }

    void remove(final InfoHash infoHash) {
        this.map.remove(infoHash);
    }

    Optional<Announcer> getAnnouncer(final InfoHash infoHash) {
        return Optional.ofNullable(this.map.get(infoHash));
    }

}
