package org.araymond.joalcore.core.sharing.domain.events;

import org.araymond.joalcore.events.DomainEvent;

public class TorrentStartedSharingEvent extends DomainEvent {
    private final boolean isFullyDownloaded;

    public TorrentStartedSharingEvent(boolean isFullyDownloaded) {
        this.isFullyDownloaded = isFullyDownloaded;
    }

    public boolean isFullyDownloaded() {
        return isFullyDownloaded;
    }
}
