package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedOrAfterStartedAnnounceRefreshKeyGeneratorTest {

    @Test
    public void shouldNotBuildWithoutRefreshEvery() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        assertThatThrownBy(() -> new TimedOrAfterStartedAnnounceRefreshKeyGenerator(null, algo, Casing.NONE))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldNotBuildWithRefreshEveryLessThanOne() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        assertThatThrownBy(() -> new TimedOrAfterStartedAnnounceRefreshKeyGenerator(0, algo, Casing.NONE))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldBuild() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final TimedOrAfterStartedAnnounceRefreshKeyGenerator generator = new TimedOrAfterStartedAnnounceRefreshKeyGenerator(10, algo, Casing.NONE);

        assertThat(generator.getRefreshEvery()).isEqualTo(10);
    }

    @Test
    public void keyShouldNotBeRefreshedIfDelayIsNotElapsedAndRefreshWhenElapsed() throws InterruptedException {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final TimedOrAfterStartedAnnounceRefreshKeyGenerator generator = new TimedOrAfterStartedAnnounceRefreshKeyGenerator(1, algo, Casing.NONE);

        final String firstKey = generator.getKey(null, RequestEvent.STOPPED);
        assertThat(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(firstKey);

        Mockito.verify(algo, Mockito.times(1)).generate();
        Mockito.when(algo.generate()).thenReturn("do-not-care2");

        Thread.sleep(500);
        generator.getKey(null, RequestEvent.STARTED);
        Thread.sleep(510);

        Mockito.verify(algo, Mockito.times(2)).generate();

        assertThat(generator.getKey(null, RequestEvent.STOPPED)).isNotEqualTo(firstKey);
    }

    @Test
    public void shouldBeRefreshAfterStartedEvent() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final TimedOrAfterStartedAnnounceRefreshKeyGenerator generator = new TimedOrAfterStartedAnnounceRefreshKeyGenerator(1, algo, Casing.NONE);

        final String firstKey = generator.getKey(null, RequestEvent.STOPPED);
        assertThat(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(firstKey);

        Mockito.verify(algo, Mockito.times(1)).generate();
        Mockito.when(algo.generate()).thenReturn("do-not-care2");

        // The key returned after the first started is still the same
        assertThat(generator.getKey(null, RequestEvent.STARTED)).isEqualTo(firstKey);
        // They key must have changed after the start
        assertThat(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isNotEqualTo(firstKey);

        Mockito.verify(algo, Mockito.times(2)).generate();
    }

}
