package com.araymond.joalcore.core.sharing.domain.services;

import com.araymond.joalcore.annotations.DomainService;
import com.araymond.joalcore.core.sharing.domain.Peers;

@FunctionalInterface
@DomainService
public interface PeerElection {
    Peers elect(Peers candidate1, Peers candidate2);

    PeerElection MOST_LEECHED = (c1, c2) -> c1.mostLeechedThan(c2) ? c1 : c2;

    PeerElection MOST_SEEDED = (c1, c2) -> c1.mostSeededThan(c2) ? c1 : c2;

    PeerElection MOST_LEECHED_NON_ZERO_SEEDERS = (c1, c2) -> {
        if (c1.hasNoSeeders() || c2.hasNoSeeders()) {
            // if one of the two peers has 0 seeders, return the one with the most seeders (the only one > 0)
            return MOST_SEEDED.elect(c1, c2);
        }
        return MOST_LEECHED.elect(c1, c2);
    };

    PeerElection MOST_SEEDED_NON_ZERO_LEECHERS = (c1, c2) -> {
        if (c1.hasNoLeechers() || c2.hasNoLeechers()) {
            return MOST_LEECHED.elect(c1, c2);
        }
        return MOST_SEEDED.elect(c1, c2);
    };
}
