package org.araymond.joalcore.core.sharing.application;

import org.araymond.joalcore.core.fixtures.TestFixtures;
import org.araymond.joalcore.core.metadata.domain.InfoHash;
import org.araymond.joalcore.core.metadata.domain.TorrentMetadata;
import org.araymond.joalcore.core.metadata.domain.TorrentSize;
import org.araymond.joalcore.core.sharing.domain.*;
import org.araymond.joalcore.core.sharing.domain.events.*;
import org.araymond.joalcore.core.sharing.domain.services.PeerElection;
import org.araymond.joalcore.events.DomainEvent;
import org.araymond.joalcore.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class SharedTorrentServiceTest {
    private InMemorySharedTorrentRepository repo;
    private FakePublisher publisher;
    private OverallContributions overallContributions;

    @BeforeEach
    public void setUp() {
        this.repo = new InMemorySharedTorrentRepository();
        this.publisher = new FakePublisher();
        this.overallContributions = new ZeroOverallContributions();
    }

    public SharedTorrentService newService() {
        return new SharedTorrentService(repo, publisher, overallContributions, new SharedTorrentConfig(
                () -> PeerElection.MOST_LEECHED,
                () -> false
        ));
    }

    @Test
    public void shouldShareAndAcceptContributions() {
        var service = newService();

        var infoHash = TestFixtures.randomInfoHash();
        service.create(new TorrentMetadata(infoHash, TorrentSize.ofBytes(5000)));

        assertThat(publisher.events).hasSizeGreaterThanOrEqualTo(1).first().isInstanceOf(TorrentCreatedEvent.class);
        var torrentId = ((TorrentCreatedEvent) publisher.events.getFirst()).sharedTorrentId();

        service.addDownload(torrentId, new DownloadAmount(1300));
        service.addUpload(torrentId, new UploadAmount(500));
        service.registerPeers(infoHash, new Swarm.TrackerUniqueIdentifier("a"), new Peers(new Leechers(50), new Seeders(30)));

        service.pause(torrentId);
        service.share(torrentId);

        service.addDownload(torrentId, new DownloadAmount(7000));

        assertThat(publisher.events.stream().map(DomainEvent::getClass))
                .isEqualTo(List.of(
                        TorrentCreatedEvent.class,
                        TorrentStartedSharingEvent.class,
                        TorrentPeersChangedEvent.class,
                        TorrentPausedEvent.class,
                        TorrentStartedSharingEvent.class,
                        DoneDownloadingEvent.class
                ));

        var torrent = repo.torrents.stream().findFirst().orElseThrow(() -> new IllegalStateException("expected a torrent to be in the repository"));
        assertThat(torrent.overallContributions()).isEqualTo(new Contribution(new DownloadAmount(5000), new UploadAmount(500)));
    }


    private static final class FakePublisher implements DomainEventPublisher {
        public final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }
    }

    private static final class ZeroOverallContributions implements OverallContributions {
        @Override
        public Optional<Contribution> load(InfoHash infoHash) {
            return Optional.empty();
        }

        @Override
        public void save(InfoHash infoHash, Contribution contribution) {
        }
    }

    private static final class InMemorySharedTorrentRepository implements SharedTorrentRepository {
        private final Collection<SharedTorrent> torrents = new HashSet<>();

        @Override
        public Optional<SharedTorrent> findById(SharedTorrentId id) {
            return torrents.stream().filter(t -> t.id().equals(id)).findFirst();
        }

        @Override
        public Optional<SharedTorrent> findByTorrentInfoHash(InfoHash hash) {
            return torrents.stream().filter(t -> t.infoHash().equals(hash)).findFirst();
        }

        @Override
        public void save(SharedTorrent torrent) {
            this.torrents.add(torrent);
        }
    }
}