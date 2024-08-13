package com.araymond.joalcore.core.sharing.domain.events;

import com.araymond.joalcore.core.sharing.domain.Peers;
import com.araymond.joalcore.core.sharing.domain.SharedTorrentId;
import com.araymond.joalcore.events.DomainEvent;

import java.util.Objects;

public class TorrentPeersChangedEvent extends DomainEvent {
    private final SharedTorrentId sharedTorrentId;
    private final Peers peers;

    public TorrentPeersChangedEvent(SharedTorrentId sharedTorrentId, Peers peers) {
        this.sharedTorrentId = sharedTorrentId;
        this.peers = peers;
    }

    public SharedTorrentId sharedTorrentId() {
        return sharedTorrentId;
    }

    public Peers peers() {
        return peers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TorrentPeersChangedEvent that = (TorrentPeersChangedEvent) o;
        return Objects.equals(sharedTorrentId, that.sharedTorrentId) && Objects.equals(peers, that.peers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sharedTorrentId, peers);
    }
}
