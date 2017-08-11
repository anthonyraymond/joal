package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedRefreshPeerIdGeneratorTest {

    @Test
    public void shouldNotBuildWithoutRefreshEvery() {
        assertThatThrownBy(() -> new TimedRefreshPeerIdGenerator(null, "-AA-", "[a-zA-Z0-9]"))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldNotBuildWithRefreshEveryLessThanOne() {
        assertThatThrownBy(() -> new TimedRefreshPeerIdGenerator(0, "-AA-", "[a-zA-Z0-9]"))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("refreshEvery must be greater than 0.");
    }

    @Test
    public void shouldBuild() {
        final TimedRefreshPeerIdGenerator generator = new TimedRefreshPeerIdGenerator(10, "-AA-", "[a-zA-Z0-9]");

        assertThat(generator.getRefreshEvery()).isEqualTo(10);
    }

    @Test
    public void peerIdShouldNotBeRefreshedIfDelayIsNotElapsedAndRefreshWhenElapsed() throws InterruptedException {
        final TimedRefreshPeerIdGenerator generator = new TimedRefreshPeerIdGenerator(1, "-AA-", "[a-zA-Z0-9]");

        final String firstKey = generator.getPeerId(null, RequestEvent.STARTED);
        assertThat(generator.getPeerId(null, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(null, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(null, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(null, RequestEvent.STARTED))
                .isEqualTo(firstKey);

        Thread.sleep(500);
        generator.getPeerId(null, RequestEvent.STARTED);
        Thread.sleep(510);

        assertThat(generator.getPeerId(null, RequestEvent.STARTED)).isNotEqualTo(firstKey);
    }

}
