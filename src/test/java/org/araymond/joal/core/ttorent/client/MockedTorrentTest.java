package org.araymond.joal.core.ttorent.client;

import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.utils.TorrentFileCreator;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 15/05/2017.
 */
public class MockedTorrentTest {

    @Test
    public void shouldBeEqualByHexInfoHash() throws IOException, NoSuchAlgorithmException {
        final MockedTorrent torrent = MockedTorrent.fromFile(TorrentFileCreator.getTorrentPath(TorrentFileCreator.TorrentType.UBUNTU).toFile());
        final MockedTorrent torrent2 = MockedTorrent.fromFile(TorrentFileCreator.getTorrentPath(TorrentFileCreator.TorrentType.UBUNTU).toFile());

        assertThat(torrent).isEqualTo(torrent2);
        assertThat(torrent.hashCode()).isEqualTo(torrent2.hashCode());
    }

    @Test
    public void shouldNotBeEqualsWithDifferentInfoHash() throws IOException, NoSuchAlgorithmException {
        final MockedTorrent torrent = MockedTorrent.fromFile(TorrentFileCreator.getTorrentPath(TorrentFileCreator.TorrentType.UBUNTU).toFile());
        final MockedTorrent torrent2 = MockedTorrent.fromFile(TorrentFileCreator.getTorrentPath(TorrentFileCreator.TorrentType.AUDIO).toFile());

        assertThat(torrent).isNotEqualTo(torrent2);
        assertThat(torrent.hashCode()).isNotEqualTo(torrent2.hashCode());
    }

}
