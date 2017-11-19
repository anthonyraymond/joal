package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
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
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator(algo, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        final String keyOne = generator.getPeerId(t1, RequestEvent.STARTED);
        assertThat(keyOne)
                .isEqualTo(generator.getPeerId(t1, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t1, RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(1)).generate();
        Mockito.when(algo.generate()).thenReturn("do-not-care2");

        final String keyTwo = generator.getPeerId(t2, RequestEvent.STARTED);
        assertThat(keyTwo)
                .isEqualTo(generator.getPeerId(t2, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t2, RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(2)).generate();
        assertThat(keyOne).isNotEqualTo(keyTwo);
    }

    @Test
    public void shouldRefreshKeyWhenTorrentHasStopped() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator(algo, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);

        generator.getPeerId(t1, RequestEvent.STARTED);
        generator.getPeerId(t1, RequestEvent.STOPPED);
        generator.getPeerId(t1, RequestEvent.STARTED);

        Mockito.verify(algo, Mockito.times(2)).generate();
    }

}
