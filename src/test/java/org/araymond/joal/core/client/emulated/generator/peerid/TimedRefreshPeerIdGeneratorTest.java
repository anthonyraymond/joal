package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedRefreshPeerIdGeneratorTest {

    @Test
    public void shouldNotBuildWithoutRefreshEvery() {
        assertThatThrownBy(() -> new TimedRefreshPeerIdGenerator(null, Mockito.mock(PeerIdAlgorithm.class), false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldNotBuildWithRefreshEveryLessThanOne() {
        assertThatThrownBy(() -> new TimedRefreshPeerIdGenerator(0, Mockito.mock(PeerIdAlgorithm.class), false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldBuild() {
        final TimedRefreshPeerIdGenerator generator = new TimedRefreshPeerIdGenerator(10, Mockito.mock(PeerIdAlgorithm.class), false);

        assertThat(generator.getRefreshEvery()).isEqualTo(10);
    }

    @Test
    public void peerIdShouldNotBeRefreshedIfDelayIsNotElapsedAndRefreshWhenElapsed() throws InterruptedException {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final TimedRefreshPeerIdGenerator generator = new TimedRefreshPeerIdGenerator(1, algo, false);

        for( int i = 0; i < 10; ++i) {
            generator.getPeerId(new InfoHash(ByteBuffer.allocate(4).putInt(i).array()), RequestEvent.STARTED);
        }
        Mockito.verify(algo, Mockito.times(1)).generate();

        Thread.sleep(500);
        generator.getPeerId(new InfoHash(ByteBuffer.allocate(4).putInt(1).array()), RequestEvent.STARTED);
        Mockito.verify(algo, Mockito.times(1)).generate();
        Thread.sleep(510);

        generator.getPeerId(new InfoHash(ByteBuffer.allocate(4).putInt(1).array()), RequestEvent.STARTED);
        Mockito.verify(algo, Mockito.times(2)).generate();
    }

}
