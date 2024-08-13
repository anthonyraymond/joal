package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.ValueObject;
import com.araymond.joalcore.core.sharing.domain.exceptions.InvalidChangeOfStateException;

@ValueObject
public enum SharingStatus {
    Sharing,
    Paused;

    SharingStatus pause() {
        assertNotAlready(Paused);
        return Paused;
    }

    SharingStatus share() {
        assertNotAlready(Sharing);
        return Sharing;
    }

    private void assertNotAlready(SharingStatus status) {
        if (status == this) {
            throw new InvalidChangeOfStateException("Torrent is already %s".formatted(this));
        }

    }
}
