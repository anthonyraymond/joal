package org.araymond.joal.core.ttorent.client.bandwidth;

import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 14/05/2017.
 */
public class TorrentWithStatsTest {

    @Test
    public void shouldNotBuildWithNullTorrent() {
        assertThatThrownBy(() -> new TorrentWithStats(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("MockedTorrent cannot be null.");
    }

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);

        final TorrentWithStats torrentWithStats = new TorrentWithStats(torrent);
        assertThat(torrentWithStats.getTorrent()).isEqualTo(torrent);
        assertThat(torrentWithStats.getUploaded()).isEqualTo(0);
        assertThat(torrentWithStats.getDownloaded()).isEqualTo(0);
        assertThat(torrentWithStats.getLeft()).isEqualTo(0);
        assertThat(torrentWithStats.getCurrentRandomSpeedInBytes()).isEqualTo(0L);
    }

    @Test
    public void shouldAddUploaded() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);

        final TorrentWithStats torrentWithStats = new TorrentWithStats(torrent);
        assertThat(torrentWithStats.getUploaded()).isEqualTo(0);
        torrentWithStats.addUploaded(4256879L);
        assertThat(torrentWithStats.getUploaded()).isEqualTo(4256879L);
        torrentWithStats.addUploaded(21L);
        assertThat(torrentWithStats.getUploaded()).isEqualTo(4256900L);
    }

    @Test
    public void shouldSetCurrentRandomSpeed() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);
        final TorrentWithStats torrentWithStats = new TorrentWithStats(torrent);

        assertThat(torrentWithStats.getCurrentRandomSpeedInBytes()).isEqualTo(0L);
        torrentWithStats.refreshRandomSpeedInBytes(50L);
        assertThat(torrentWithStats.getCurrentRandomSpeedInBytes()).isEqualTo(50L);
        torrentWithStats.refreshRandomSpeedInBytes(70L);
        assertThat(torrentWithStats.getCurrentRandomSpeedInBytes()).isEqualTo(70L);
    }

    @Test
    public void shouldBeEqualByTorrent() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);

        final TorrentWithStats torrentWithStats = new TorrentWithStats(torrent);
        final TorrentWithStats torrentWithStats2 = new TorrentWithStats(torrent);
        assertThat(torrentWithStats).isEqualTo(torrentWithStats2);
        assertThat(torrentWithStats.hashCode()).isEqualTo(torrentWithStats2.hashCode());
    }

    @Test
    public void shouldNotBeEqualsWithDifferentTorrent() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);
        final MockedTorrent torrent2 = Mockito.mock(MockedTorrent.class);

        final TorrentWithStats torrentWithStats = new TorrentWithStats(torrent);
        final TorrentWithStats torrentWithStats2 = new TorrentWithStats(torrent2);
        assertThat(torrentWithStats).isNotEqualTo(torrentWithStats2);
        assertThat(torrentWithStats.hashCode()).isNotEqualTo(torrentWithStats2.hashCode());
    }

}
