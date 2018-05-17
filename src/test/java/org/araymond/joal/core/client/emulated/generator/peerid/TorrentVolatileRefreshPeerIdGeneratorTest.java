package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentVolatileRefreshPeerIdGeneratorTest {

    @Test
    public void shouldHaveOneKeyPerTorrent() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care-too-much");
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator(algo, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 22 })).when(t1).getTorrentInfoHash();
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 42 })).when(t2).getTorrentInfoHash();

        final String keyOne = generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED);
        assertThat(keyOne)
                .isEqualTo(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(1)).generate();
        Mockito.when(algo.generate()).thenReturn("!!-not-care-too-much");

        final String keyTwo = generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STARTED);
        assertThat(keyTwo)
                .isEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(2)).generate();
        assertThat(keyOne).isNotEqualTo(keyTwo);
    }

    @Test
    public void shouldRefreshKeyWhenTorrentHasStopped() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care-too-much");
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator(algo, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 22 })).when(t1).getTorrentInfoHash();

        generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED);
        generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STOPPED);
        generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED);

        Mockito.verify(algo, Mockito.times(2)).generate();
    }

}
