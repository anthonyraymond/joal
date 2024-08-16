package org.araymond.joalcore.core.sharing.application;

import org.araymond.joalcore.core.sharing.domain.services.PeerElection;

import java.util.function.Supplier;

public class SharedTorrentConfig {
    private final Supplier<PeerElection> peersElection;
    private final Supplier<Boolean> skipDownload;

    public SharedTorrentConfig(Supplier<PeerElection> peersElection, Supplier<Boolean> skipDownload) {
        this.peersElection = peersElection;
        this.skipDownload = skipDownload;
    }

    public Supplier<PeerElection> peersElection() {
        return peersElection;
    }

    public Supplier<Boolean> skipDownload() {
        return skipDownload;
    }
}
