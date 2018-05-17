package org.araymond.joal.core.bandwith;

import com.google.common.base.Objects;

public class Peers {
    private final int seeders;
    private final int leechers;
    private final float leechersRatio;

    public Peers(final int seeders, final int leechers) {
        this.seeders = seeders;
        this.leechers = leechers;
        this.leechersRatio = (this.seeders + this.leechers) == 0
                ? 0
                : ((float) this.leechers) / (this.seeders + this.leechers);
    }

    public int getSeeders() {
        return seeders;
    }

    public int getLeechers() {
        return leechers;
    }

    public float getLeechersRatio() {
        return leechersRatio;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Peers peers = (Peers) o;
        return seeders == peers.seeders &&
                leechers == peers.leechers;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seeders, leechers);
    }
}
