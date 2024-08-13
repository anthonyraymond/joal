package com.araymond.joalcore.core.sharing.application;

import com.araymond.joalcore.core.infohash.domain.InfoHash;
import com.araymond.joalcore.core.sharing.domain.Contribution;

import java.util.Optional;

public interface PersistentStats {
    Optional<Contribution> overallContributions(InfoHash infoHash);
}
