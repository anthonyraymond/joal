package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.ValueObject;
import com.araymond.joalcore.core.sharing.domain.exceptions.InvalidChangeOfStateException;

@ValueObject
public enum SharingStatus {
    Downloading,
    Seeding,
    Paused;

    SharingStatus pause() {
        assertNotAlready(Paused);
        return Paused;
    }

    SharingStatus download() {
        assertNotAlready(Downloading);
        return Downloading;
    }

    SharingStatus seed() {
        assertNotAlready(Seeding);
        return Seeding;
    }

    private void assertNotAlready(SharingStatus status) {
        if (status == this) {
            throw new InvalidChangeOfStateException("Torrent is already %s".formatted(this));
        }

    }
}
