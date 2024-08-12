package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.AggregateRoot;
import com.araymond.joalcore.annotations.ddd.ValueObject;
import com.araymond.joalcore.core.infohash.domain.InfoHash;
import com.araymond.joalcore.core.sharing.domain.events.DoneDownloadingEvent;
import com.araymond.joalcore.core.sharing.domain.events.TorrentPausedEvent;
import com.araymond.joalcore.core.sharing.domain.events.TorrentStartedDownloadingEvent;
import com.araymond.joalcore.core.sharing.domain.events.TorrentStartedSeedingEvent;
import com.araymond.joalcore.core.sharing.domain.exceptions.IllegalActionForTorrentState;
import com.araymond.joalcore.events.DomainEvent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record SharedTorrentId(UUID id) {
    public static SharedTorrentId random() {
        return new SharedTorrentId(UUID.randomUUID());
    }

    public SharedTorrentId {
        Objects.requireNonNull(id, "SharedTorrentId requires a non-null [id]");
    }
}