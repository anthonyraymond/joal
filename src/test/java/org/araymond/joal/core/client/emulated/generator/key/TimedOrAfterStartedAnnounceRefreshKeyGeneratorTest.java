package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedOrAfterStartedAnnounceRefreshKeyGeneratorTest {

    @Test
    public void shouldNotBuildWithoutRefreshEvery() {
        assertThatThrownBy(() -> new TimedOrAfterStartedAnnounceRefreshKeyGenerator(null, 8, KeyTypes.HASH, Casing.NONE))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldNotBuildWithRefreshEveryLessThanOne() {
        assertThatThrownBy(() -> new TimedOrAfterStartedAnnounceRefreshKeyGenerator(0, 8, KeyTypes.HASH, Casing.NONE))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldBuild() {
        final TimedOrAfterStartedAnnounceRefreshKeyGenerator generator = new TimedOrAfterStartedAnnounceRefreshKeyGenerator(10, 8, KeyTypes.HASH, Casing.NONE);

        assertThat(generator.getRefreshEvery()).isEqualTo(10);
    }

    @Test
    public void keyShouldNotBeRefreshedIfDelayIsNotElapsedAndRefreshWhenElapsed() throws InterruptedException {
        final TimedOrAfterStartedAnnounceRefreshKeyGenerator generator = new TimedOrAfterStartedAnnounceRefreshKeyGenerator(1, 8, KeyTypes.HASH, Casing.NONE);

        final String firstKey = generator.getKey(null, RequestEvent.STOPPED);
        assertThat(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(firstKey);

        Thread.sleep(500);
        generator.getKey(null, RequestEvent.STOPPED);
        Thread.sleep(510);

        assertThat(generator.getKey(null, RequestEvent.STOPPED)).isNotEqualTo(firstKey);
    }

    @Test
    public void shouldBeRefreshAfterStartedEvent() {
        final TimedOrAfterStartedAnnounceRefreshKeyGenerator generator = new TimedOrAfterStartedAnnounceRefreshKeyGenerator(1, 8, KeyTypes.HASH, Casing.NONE);

        final String firstKey = generator.getKey(null, RequestEvent.STOPPED);
        assertThat(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(firstKey);

        // The key returned after the first started is still the same
        assertThat(generator.getKey(null, RequestEvent.STARTED)).isEqualTo(firstKey);
        // They key must have changed after the start
        assertThat(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isEqualTo(generator.getKey(null, RequestEvent.STOPPED))
                .isNotEqualTo(firstKey);
    }

}
