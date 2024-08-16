package org.araymond.joalcore.core.fixtures;

import org.araymond.joalcore.core.metadata.domain.InfoHash;
import org.araymond.joalcore.core.sharing.domain.*;

import java.util.Random;

public class TestFixtures {
    public static InfoHash randomInfoHash() {
        var hash = new byte[20];
        new Random().nextBytes(hash);
        return new InfoHash(hash);
    }

    public static SharedTorrent sharedTorrent(Contribution overall, Left left) {
        return new SharedTorrent(randomInfoHash(), overall, left);
    }

    public static SharedTorrent zeroContribSharedTorrent(Left left) {
        return sharedTorrent(Contribution.ZERO, left);
    }

    public static SharedTorrent fullyDownloadedSharedTorrent() {
        return sharedTorrent(new Contribution(new DownloadAmount(500), new UploadAmount(0)), Left.ZERO);
    }
}
