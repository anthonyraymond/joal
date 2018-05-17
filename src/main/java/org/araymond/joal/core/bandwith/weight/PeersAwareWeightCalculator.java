package org.araymond.joal.core.bandwith.weight;

import org.araymond.joal.core.bandwith.Peers;

public class PeersAwareWeightCalculator {
    public double calculate(final Peers peers) {
        if (peers.getSeeders() == 0) {
            return 0.0;
        }
        final double leechersRatio = peers.getLeechersRatio();
        if (leechersRatio == 0) {
            return 0.0;
        }
        return leechersRatio * 100
                * (peers.getSeeders() * leechersRatio)
                * (((double) peers.getLeechers()) / peers.getSeeders());
    }
}
