package org.araymond.joalcore.core.sharing.application;

import org.araymond.joalcore.core.infohash.domain.InfoHash;
import org.araymond.joalcore.core.sharing.domain.Contribution;

import java.util.Optional;

public interface PersistentStats {
    Optional<Contribution> overallContributions(InfoHash infoHash);

    /*
    TODO: implementation should have the while map in memory. a call to persistOverallContribution update the map.
      Once in a while, the map is wrote to the disk asynchronously
     */
    void persistOverallContribution(InfoHash infoHash, Contribution contribution);
}
