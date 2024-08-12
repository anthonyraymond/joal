package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.core.fixtures.TestFixtures;
import com.araymond.joalcore.core.sharing.domain.events.*;
import com.araymond.joalcore.core.sharing.domain.exceptions.IllegalActionForTorrentState;
import com.araymond.joalcore.core.sharing.domain.services.PeerElection;
import com.araymond.joalcore.events.DomainEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SharedTorrentTest {

    @Test
    public void shouldDenyAddDownloadWhenTorrentIsPaused() {
        var torrent = TestFixtures.fullyDownloadedSharedTorrent();
        if (!torrent.isPaused()) torrent.pause();

        assertThatThrownBy(() -> torrent.add(new DownloadAmount(1)))
                .isInstanceOf(IllegalActionForTorrentState.class);
    }

    @Test
    public void shouldAllowAddDownloadWhenTorrentIsDownloading() {
        var torrent = TestFixtures.zeroContribSharedTorrent(new Left(500));
        torrent.download();

        torrent.add(new DownloadAmount(1));
    }

    @Test
    public void shouldDenyAddDownloadWhenTorrentIsSeeding() {
        var torrent = TestFixtures.fullyDownloadedSharedTorrent();
        torrent.seed();

        assertThatThrownBy(() -> torrent.add(new DownloadAmount(1)))
                .isInstanceOf(IllegalActionForTorrentState.class);
    }

    @Test
    public void shouldDenyAddUploadWhenTorrentIsPaused() {
        var torrent = TestFixtures.zeroContribSharedTorrent(new Left(500));
        if (!torrent.isPaused()) torrent.pause();

        assertThatThrownBy(() -> torrent.add(new UploadAmount(1)))
                .isInstanceOf(IllegalActionForTorrentState.class);
    }

    @Test
    public void shouldAllowAddUploadWhenTorrentIsDownloading() {
        var torrent = TestFixtures.zeroContribSharedTorrent(new Left(500));
        torrent.download();

        torrent.add(new UploadAmount(1));
    }

    @Test
    public void shouldAllowAddUploadWhenTorrentIsSeeding() {
        var torrent = TestFixtures.fullyDownloadedSharedTorrent();
        torrent.seed();

        torrent.add(new UploadAmount(1));
    }

    @Test
    public void shouldGoInSeedingStateWhenDownloadIsFinished() {
        var torrent = TestFixtures.zeroContribSharedTorrent(new Left(500));
        torrent.download();

        List<DomainEvent> events = torrent.add(new DownloadAmount(800));

        assertThat(events.stream().map(DomainEvent::getClass))
                .isEqualTo(List.of(
                        DoneDownloadingEvent.class,
                        TorrentStartedSeedingEvent.class
                ));
    }

    @Test
    public void resumeShouldGoInDownloadingStateWhileLeftGreaterThan0() {
        Collection<DomainEvent> events = new ArrayList<>();

        var torrent = TestFixtures.zeroContribSharedTorrent(new Left(500));
        events.addAll(torrent.download());

        events.addAll(torrent.pause());
        events.addAll(torrent.resume());
        assertThat(torrent.isDownloading()).isTrue();

        events.addAll(torrent.add(new DownloadAmount(500)));
        events.addAll(torrent.pause());
        events.addAll(torrent.resume());
        assertThat(torrent.isSeeding()).isTrue();

        assertThat(events.stream().map(DomainEvent::getClass))
                .isEqualTo(List.of(
                        TorrentStartedDownloadingEvent.class,
                        TorrentPausedEvent.class,
                        TorrentStartedDownloadingEvent.class,
                        DoneDownloadingEvent.class,
                        TorrentStartedSeedingEvent.class,
                        TorrentPausedEvent.class,
                        TorrentStartedSeedingEvent.class
                ));
    }

    @Test
    public void shouldRegisterPeers() {
        var torrent = TestFixtures.fullyDownloadedSharedTorrent();
        
        var events = torrent.registerPeers(new Swarm.TrackerUniqueIdentifier("a"), new Peers(new Leechers(10), new Seeders(1)), PeerElection.MOST_LEECHED);

        assertThat(events)
                .first().isInstanceOf(TorrentPeersChangedEvent.class)
                .extracting("peers").isEqualTo(new Peers(new Leechers(10), new Seeders(1)));
    }

}