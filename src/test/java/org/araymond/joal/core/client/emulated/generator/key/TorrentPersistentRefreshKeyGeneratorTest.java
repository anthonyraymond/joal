package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.client.emulated.generator.key.TorrentPersistentRefreshKeyGenerator.AccessAwareKey;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentPersistentRefreshKeyGeneratorTest {

    @Test
    public void shouldHaveOneKeyPerTorrent() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(algo, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        assertThat(generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE))
                .isEqualTo(generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED));
        Mockito.verify(algo, Mockito.times(1)).generate();

        assertThat(generator.getKey(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE))
                .isEqualTo(generator.getKey(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED));
        Mockito.verify(algo, Mockito.times(2)).generate();
    }

    @Test
    public void shouldNotRefreshKeyWhenTorrentHasStopped() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final KeyGenerator generator = new TorrentPersistentRefreshKeyGenerator(algo, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);

        final String key1 = generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED);
        final String key2 = generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED);
        final String key3 = generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED);

        Mockito.verify(algo, Mockito.times(1)).generate();

        assertThat(key1)
                .isEqualTo(key2)
                .isEqualTo(key3);
    }

    @Test
    public void shouldNotHaveSameKeyForAllTorrent() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final KeyGenerator generator = new TorrentPersistentRefreshKeyGenerator(algo, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        final String key1 = generator.getKey(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED);
        Mockito.when(algo.generate()).thenReturn("do-not-care2");
        final String key2 = generator.getKey(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED);

        Mockito.verify(algo, Mockito.times(2)).generate();

        assertThat(key1)
                .isNotEqualTo(key2);
    }

    @Test
    public void shouldConsiderEntryEvictableIfOlderThanOneHourAndAHalf() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final TorrentPersistentRefreshKeyGenerator generator = new TorrentPersistentRefreshKeyGenerator(algo, Casing.NONE);

        final AccessAwareKey oldKey = Mockito.mock(AccessAwareKey.class);
        Mockito.when(oldKey.getLastAccess()).thenReturn(LocalDateTime.now().minus(120, ChronoUnit.MINUTES));
        Mockito.when(oldKey.getPeerId()).thenReturn("-BT-C-");
        assertThat(generator.shouldEvictEntry(new AbstractMap.SimpleEntry<>(Mockito.mock(MockedTorrent.class), oldKey))).isTrue();
    }

    @Test
    public void shouldNotConsiderEntryEvictableIfYoungerThanOneHourAndAHalf() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final TorrentPersistentRefreshKeyGenerator generator = new TorrentPersistentRefreshKeyGenerator(algo, Casing.NONE);

        final AccessAwareKey oldKey = Mockito.mock(AccessAwareKey.class);
        Mockito.when(oldKey.getLastAccess()).thenReturn(LocalDateTime.now().minus(119, ChronoUnit.MINUTES));
        Mockito.when(oldKey.getPeerId()).thenReturn("-BT-C-");
        assertThat(generator.shouldEvictEntry(new AbstractMap.SimpleEntry<>(Mockito.mock(MockedTorrent.class), oldKey))).isFalse();
    }

}
