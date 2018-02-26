package org.araymond.joal.core.torrent.torrent;

import com.google.common.collect.Lists;
import com.turn.ttorrent.common.Torrent;
import org.araymond.joal.core.utils.TorrentFileCreator;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class MockedTorrentTest {

    public static MockedTorrent createOneMock() {
        return createOneMock("abcdefghij");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static MockedTorrent createOneMock(final String infoHash) {
        final MockedTorrent torrent = mock(MockedTorrent.class);
        doReturn(new InfoHash(infoHash.getBytes())).when(torrent).getTorrentInfoHash();
        doReturn(infoHash.getBytes()).when(torrent).getInfoHash();

        final List<List<URI>> uris = Lists.newArrayList();
        uris.add(Lists.newArrayList(URI.create("http://localhost"), URI.create("https://localhost")));
        uris.add(Lists.newArrayList(URI.create("http://127.0.0.1"), URI.create("https://127.0.0.1")));
        doReturn(uris).when(torrent).getAnnounceList();
        doReturn(Torrent.byteArrayToHexString(infoHash.getBytes())).when(torrent).getHexInfoHash();
        doReturn("generic torrent").when(torrent).getName();
        doReturn(1234567L).when(torrent).getSize();
        doReturn(2).when(torrent).getTrackerCount();

        return torrent;
    }

    @Test
    public void shouldBeEqualByInfoHash() throws IOException, NoSuchAlgorithmException {
        final MockedTorrent torrent1 = MockedTorrent.fromFile(TorrentFileCreator.getTorrentPath(TorrentFileCreator.TorrentType.UBUNTU).toFile());
        final MockedTorrent torrent2 = MockedTorrent.fromFile(TorrentFileCreator.getTorrentPath(TorrentFileCreator.TorrentType.UBUNTU).toFile());
        final MockedTorrent torrent3 = MockedTorrent.fromFile(TorrentFileCreator.getTorrentPath(TorrentFileCreator.TorrentType.AUDIO).toFile());

        assertThat(torrent1)
                .isEqualTo(torrent2)
                .isNotEqualTo(torrent3);

        assertThat(torrent1.hashCode())
                .isEqualTo(torrent2.hashCode())
                .isNotEqualTo(torrent3.hashCode());
    }

}
