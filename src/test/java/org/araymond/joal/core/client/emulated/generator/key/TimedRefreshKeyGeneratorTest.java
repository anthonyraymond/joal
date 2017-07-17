package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.StringTypes;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedRefreshKeyGeneratorTest {

    @Test
    public void shouldNotBuildWithoutRefreshEvery() {
        assertThatThrownBy(() -> new TimedRefreshKeyGenerator(null, 8, StringTypes.ALPHANUMERIC, false, false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldNotBuildWithRefreshEveryLessThanOne() {
        assertThatThrownBy(() -> new TimedRefreshKeyGenerator(0, 8, StringTypes.ALPHANUMERIC, false, false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldBuild() {
        final TimedRefreshKeyGenerator generator = new TimedRefreshKeyGenerator(10, 8, StringTypes.ALPHANUMERIC, false, false);

        assertThat(generator.getRefreshEvery()).isEqualTo(10);
    }

    @Test
    public void keyShouldNotBeRefreshedIfDelayIsNotElapsed() throws InterruptedException {
        final TimedRefreshKeyGenerator generator = new TimedRefreshKeyGenerator(1, 8, StringTypes.ALPHANUMERIC, false, false);

        final String firstKey = generator.getKey(null, RequestEvent.STARTED);
        assertThat(generator.getKey(null, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(null, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(null, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(null, RequestEvent.STARTED))
                .isEqualTo(firstKey);
        Thread.sleep(1100);

        assertThat(generator.getKey(null, RequestEvent.STARTED)).isNotEqualTo(firstKey);
    }

}
