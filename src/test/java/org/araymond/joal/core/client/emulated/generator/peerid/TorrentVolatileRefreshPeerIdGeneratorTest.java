package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentVolatileRefreshPeerIdGeneratorTest {

    @Test
    public void shouldHaveOneKeyPerTorrent() {
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator("-AA-", "[a-zA-Z0-9]", false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        assertThat(generator.getPeerId(t1, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t1, RequestEvent.STOPPED));

        assertThat(generator.getPeerId(t2, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t2, RequestEvent.STOPPED));
    }

    @Test
    public void shouldRefreshKeyWhenTorrentHasStopped() {
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator("-AA-", "[a-zA-Z0-9]", false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);

        final String key1 = generator.getPeerId(t1, RequestEvent.STARTED);
        final String key2 = generator.getPeerId(t1, RequestEvent.STOPPED);
        final String key3 = generator.getPeerId(t1, RequestEvent.NONE);

        assertThat(key1)
                .isEqualTo(key2)
                .isNotEqualTo(key3);
    }

    @Test
    public void shouldNotHaveSameKeyForAllTorrent() {
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator("-AA-", "[a-zA-Z0-9]", false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        assertThat(generator.getPeerId(t1, RequestEvent.STARTED))
                .isNotEqualTo(generator.getPeerId(t2, RequestEvent.STARTED));
    }

}
