package org.araymond.joalcore.core.sharing.application;

import org.araymond.joalcore.config.ConfigSupplier;
import org.araymond.joalcore.core.metadata.domain.InfoHash;
import org.araymond.joalcore.core.metadata.domain.TorrentMetadata;
import org.araymond.joalcore.core.sharing.domain.Contribution;
import org.araymond.joalcore.core.sharing.domain.DownloadAmount;
import org.araymond.joalcore.core.sharing.domain.UploadAmount;

public class OverallContributionsLoader {
    private final OverallContributionsRepository repo;
    private final ConfigSupplier<Boolean> skipDownload;

    public OverallContributionsLoader(OverallContributionsRepository overallContributions, ConfigSupplier<Boolean> skipDownload) {
        this.repo = overallContributions;
        this.skipDownload = skipDownload;
    }

    public Contribution load(TorrentMetadata metadata) {
        return this.repo.load(metadata.infoHash())
                .orElseGet(() -> {
                    Contribution overall = Contribution.ZERO;
                    if (skipDownload.get()) {
                        // return a fully Downloaded contribution when the torrent is not yet known and skip download is true
                        overall = new Contribution(new DownloadAmount(metadata.size().bytes()), new UploadAmount(0));
                    }
                    this.repo.save(metadata.infoHash(), overall);

                    return overall;
                });
    }

    public void save(InfoHash infoHash, Contribution contribution){
        repo.save(infoHash, contribution);
    }

}
