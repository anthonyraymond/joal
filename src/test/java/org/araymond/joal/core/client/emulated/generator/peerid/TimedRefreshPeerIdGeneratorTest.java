package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedRefreshPeerIdGeneratorTest {

    @Test
    public void shouldNotBuildWithRefreshEveryLessThanOne() {
        assertThatThrownBy(() -> new TimedRefreshPeerIdGenerator(0, Mockito.mock(PeerIdAlgorithm.class), false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0");
    }

    @Test
    public void shouldBuild() {
        final TimedRefreshPeerIdGenerator generator = new TimedRefreshPeerIdGenerator(10, Mockito.mock(PeerIdAlgorithm.class), false);

        assertThat(generator.getRefreshEvery()).isEqualTo(10);
    }

    @Test
    public void peerIdShouldNotBeRefreshedIfDelayIsNotElapsedAndRefreshWhenElapsed() throws InterruptedException {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care-too-much");
        final TimedRefreshPeerIdGenerator generator = new TimedRefreshPeerIdGenerator(1, algo, false);

        final InfoHash infoHash = new InfoHash(new byte[] { 22 });
        for (int i = 0; i < 10; ++i) {
            generator.getPeerId(infoHash, RequestEvent.STARTED);
        }
        Mockito.verify(algo, times(1)).generate();

        generator.lastGeneration = LocalDateTime.now().minus(10, ChronoUnit.SECONDS);

        generator.getPeerId(infoHash, RequestEvent.STARTED);
        Mockito.verify(algo, times(2)).generate();
    }

}
