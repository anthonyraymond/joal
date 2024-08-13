package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.ValueObject;

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