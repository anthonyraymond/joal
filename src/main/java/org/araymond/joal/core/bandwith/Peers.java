package org.araymond.joal.core.bandwith;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = {"seeders", "leechers"})
@Getter
public class Peers {
    private final int seeders;
    private final int leechers;
    private final float leechersRatio;

    public Peers(final int seeders, final int leechers) {
        this.seeders = seeders;
        this.leechers = leechers;

        var allPeers = this.seeders + this.leechers;
        this.leechersRatio = allPeers == 0
                ? 0
                : ((float) this.leechers) / allPeers;
    }
}
