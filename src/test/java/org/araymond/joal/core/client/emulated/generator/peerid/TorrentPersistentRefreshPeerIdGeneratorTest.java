package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.client.emulated.generator.StringTypes;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;

import static org.araymond.joal.core.client.emulated.generator.peerid.TorrentPersistentRefreshPeerIdGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentPersistentRefreshPeerIdGeneratorTest {

    @Test
    public void shouldHaveOneKeyPerTorrent() {
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator("-AA-", StringTypes.ALPHANUMERIC, false, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        assertThat(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED));

        assertThat(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED));
    }

    @Test
    public void shouldNotRefreshKeyWhenTorrentHasStopped() {
        final PeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator("-AA-", StringTypes.ALPHANUMERIC, false, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);

        final String key1 = generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED);
        final String key2 = generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED);
        final String key3 = generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE);

        assertThat(key1)
                .isEqualTo(key2)
                .isEqualTo(key3);
    }

    @Test
    public void shouldNotHaveSameKeyForAllTorrent() {
        final PeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator("-AA-", StringTypes.ALPHANUMERIC, false, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        assertThat(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isNotEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED));
    }

    @Test
    public void shouldConsiderEntryEvictableIfOlderThanOneHourAndAHalf() {
        final TorrentPersistentRefreshPeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator("-AA-", StringTypes.ALPHANUMERIC, false, false);

        final AccessAwarePeerId oldKey = Mockito.mock(AccessAwarePeerId.class);
        Mockito.when(oldKey.getLastAccess()).thenReturn(LocalDateTime.now().minus(90, ChronoUnit.MINUTES));
        Mockito.when(oldKey.getPeerId()).thenReturn("-BT-C-");
        assertThat(generator.shouldEvictEntry(new AbstractMap.SimpleEntry<>(Mockito.mock(MockedTorrent.class), oldKey))).isTrue();
    }

    @Test
    public void shouldNotConsiderEntryEvictableIfYoungerThanOneHourAndAHalf() {
        final TorrentPersistentRefreshPeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator("-AA-", StringTypes.ALPHANUMERIC, false, false);

        final AccessAwarePeerId oldKey = Mockito.mock(AccessAwarePeerId.class);
        Mockito.when(oldKey.getLastAccess()).thenReturn(LocalDateTime.now().minus(89, ChronoUnit.MINUTES));
        Mockito.when(oldKey.getPeerId()).thenReturn("-BT-C-");
        assertThat(generator.shouldEvictEntry(new AbstractMap.SimpleEntry<>(Mockito.mock(MockedTorrent.class), oldKey))).isFalse();
    }

}
