package org.araymond.joalcore.core.sharing.domain.events;

import org.araymond.joalcore.core.sharing.domain.SharedTorrentId;
import org.araymond.joalcore.events.DomainEvent;

public class TorrentCreatedEvent extends DomainEvent {
    private final SharedTorrentId sharedTorrentId;

    public TorrentCreatedEvent(SharedTorrentId sharedTorrentId) {
        this.sharedTorrentId = sharedTorrentId;
    }

    public SharedTorrentId sharedTorrentId() {
        return sharedTorrentId;
    }
}
