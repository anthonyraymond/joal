package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.core.fixtures.TestFixtures;
import com.araymond.joalcore.core.sharing.domain.events.DoneDownloadingEvent;
import com.araymond.joalcore.core.sharing.domain.events.TorrentPausedEvent;
import com.araymond.joalcore.core.sharing.domain.events.TorrentPeersChangedEvent;
import com.araymond.joalcore.core.sharing.domain.exceptions.IllegalActionForTorrentState;
import com.araymond.joalcore.core.sharing.domain.services.PeerElection;
import com.araymond.joalcore.events.DomainEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public void shouldAllowAddDownloadAndUploadWhenTorrentIsSharing() {
        var torrent = TestFixtures.zeroContribSharedTorrent(new Left(500));
        torrent.share();

        torrent.add(new DownloadAmount(1));
        torrent.add(new UploadAmount(2));

        assertThat(torrent.overallContributions()).isEqualTo(new Contribution(new DownloadAmount(1), new UploadAmount(2)));
    }

    @Test
    public void shouldDenyDownloadWhenTorrentPaused() {
        var torrent = TestFixtures.fullyDownloadedSharedTorrent();
        if (!torrent.isPaused()) torrent.pause();

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
    public void shouldPublishDoneDownloadingWhenLeftReachZero() {
        var torrent = TestFixtures.zeroContribSharedTorrent(new Left(500));
        torrent.share();

        List<DomainEvent> events = torrent.add(new DownloadAmount(800));

        assertThat(events.stream().map(DomainEvent::getClass))
                .isEqualTo(List.of(DoneDownloadingEvent.class));
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