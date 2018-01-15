package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedRefreshKeyGeneratorTest {

    @Test
    public void shouldNotBuildWithoutRefreshEvery() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        assertThatThrownBy(() -> new TimedRefreshKeyGenerator(null, algo, Casing.NONE))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldNotBuildWithRefreshEveryLessThanOne() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        assertThatThrownBy(() -> new TimedRefreshKeyGenerator(0, algo, Casing.NONE))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldBuild() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final TimedRefreshKeyGenerator generator = new TimedRefreshKeyGenerator(10, algo, Casing.NONE);

        assertThat(generator.getRefreshEvery()).isEqualTo(10);
    }

    @Test
    public void keyShouldNotBeRefreshedIfDelayIsNotElapsedAndRefreshWhenElapsed() throws InterruptedException {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final TimedRefreshKeyGenerator generator = new TimedRefreshKeyGenerator(1, algo, Casing.NONE);

        final InfoHash infoHash = new InfoHash(new byte[] { 22 });
        final String firstKey = generator.getKey(infoHash, RequestEvent.STARTED);
        assertThat(generator.getKey(null, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(infoHash, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(infoHash, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(infoHash, RequestEvent.STARTED))
                .isEqualTo(firstKey);

        Mockito.verify(algo, Mockito.times(1)).generate();
        Mockito.when(algo.generate()).thenReturn("do-not-care2");

        Thread.sleep(500);
        generator.getKey(infoHash, RequestEvent.STARTED);
        Thread.sleep(510);

        Mockito.verify(algo, Mockito.times(1)).generate();
        assertThat(generator.getKey(infoHash, RequestEvent.STARTED)).isNotEqualTo(firstKey);
    }

}
