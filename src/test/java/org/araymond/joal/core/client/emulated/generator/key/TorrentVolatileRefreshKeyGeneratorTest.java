package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentVolatileRefreshKeyGeneratorTest {

    @Test
    public void shouldHaveOneKeyPerTorrent() {
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(8, KeyTypes.HASH, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        assertThat(generator.getKey(t1, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1, RequestEvent.NONE))
                .isEqualTo(generator.getKey(t1, RequestEvent.STOPPED));

        assertThat(generator.getKey(t2, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2, RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2, RequestEvent.NONE))
                .isEqualTo(generator.getKey(t2, RequestEvent.STOPPED));
    }

    @Test
    public void shouldRefreshKeyWhenTorrentHasStopped() {
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(8, KeyTypes.HASH, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);

        final String key1 = generator.getKey(t1, RequestEvent.STARTED);
        final String key2 = generator.getKey(t1, RequestEvent.STOPPED);
        final String key3 = generator.getKey(t1, RequestEvent.NONE);

        assertThat(key1)
                .isEqualTo(key2)
                .isNotEqualTo(key3);
    }

    @Test
    public void shouldNotHaveSameKeyForAllTorrent() {
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(8, KeyTypes.HASH, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        assertThat(generator.getKey(t1, RequestEvent.STARTED))
                .isNotEqualTo(generator.getKey(t2, RequestEvent.STARTED));
    }

}
