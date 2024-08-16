package org.araymond.joalcore.core.sharing.domain;

import org.araymond.joalcore.core.infohash.domain.InfoHash;

import java.util.Optional;

public interface SharedTorrentRepository {
    Optional<SharedTorrent> findById(SharedTorrentId id);

    Optional<SharedTorrent> findByTorrentInfoHash(InfoHash hash);

    void save(SharedTorrent torrent);
}
