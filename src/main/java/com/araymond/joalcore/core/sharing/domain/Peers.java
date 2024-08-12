package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.ValueObject;

@ValueObject
public record Peers(Leechers leechers, Seeders seeders) {
    public boolean mostLeechedThan(Peers other) {
        return this.leechers().count() >= other.leechers().count();
    }

    public boolean mostSeededThan(Peers other) {
        return this.seeders().count() >= other.seeders().count();
    }

    public boolean hasNoSeeders() {
        return seeders.count() == 0;
    }

    public boolean hasNoLeechers() {
        return leechers.count() == 0;
    }
}
