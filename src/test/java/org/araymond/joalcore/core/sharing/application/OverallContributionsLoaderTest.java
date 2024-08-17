package org.araymond.joalcore.core.sharing.application;

import org.araymond.joalcore.config.ConfigSupplier;
import org.araymond.joalcore.core.fixtures.TestFixtures;
import org.araymond.joalcore.core.metadata.domain.InfoHash;
import org.araymond.joalcore.core.metadata.domain.TorrentMetadata;
import org.araymond.joalcore.core.metadata.domain.TorrentSize;
import org.araymond.joalcore.core.sharing.domain.Contribution;
import org.araymond.joalcore.core.sharing.domain.DownloadAmount;
import org.araymond.joalcore.core.sharing.domain.UploadAmount;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OverallContributionsLoaderTest {
    private static final ConfigSupplier<Boolean> SKIP_DOWNLOAD = () -> true;
    private static final ConfigSupplier<Boolean> DO_NOT_SKIP_DOWNLOAD = () -> false;

    @Test
    public void shouldReturnContributionFullyDownloadedWhenTorrentIsNotKnownYetAndSkipDownloadIsTrue() {
        var loader = new OverallContributionsLoader(new DumbRepo(), SKIP_DOWNLOAD);
        var metadata = new TorrentMetadata(TestFixtures.randomInfoHash(), TorrentSize.ofBytes(7000));
        var contrib = loader.load(metadata);

        assertThat(contrib).isEqualTo(new Contribution(
                new DownloadAmount(metadata.size().bytes()),
                new UploadAmount(0)
        ));
    }

    @Test
    public void shouldReturnContributionZeroWhenTorrentIsNotKnownYetAndSkipDownloadIsFalse() {
        var loader = new OverallContributionsLoader(new DumbRepo(), DO_NOT_SKIP_DOWNLOAD);
        var metadata = new TorrentMetadata(TestFixtures.randomInfoHash(), TorrentSize.ofBytes(7000));
        var contrib = loader.load(metadata);

        assertThat(contrib).isEqualTo(Contribution.ZERO);
    }

    @Test
    public void shouldReturnKnownContributionZeroWhenTorrentIsKnown() {
        var repo = new DumbRepo();
        var loader = new OverallContributionsLoader(repo, DO_NOT_SKIP_DOWNLOAD);
        var metadata = new TorrentMetadata(TestFixtures.randomInfoHash(), TorrentSize.ofBytes(7000));

        repo.contributions.put(metadata.infoHash(), new Contribution(new DownloadAmount(400), new UploadAmount(545)));

        var contrib = loader.load(metadata);

        assertThat(contrib).isEqualTo(new Contribution(new DownloadAmount(400), new UploadAmount(545)));
    }

    private static final class DumbRepo implements OverallContributionsRepository {
        private final Map<InfoHash, Contribution> contributions = new HashMap<>();

        @Override
        public Optional<Contribution> load(InfoHash infoHash) {
            return Optional.ofNullable(contributions.get(infoHash));
        }

        @Override
        public void save(InfoHash infoHash, Contribution contribution) {
            contributions.put(infoHash, contribution);
        }
    }
}