package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;

import static org.araymond.joal.core.client.emulated.generator.peerid.TorrentPersistentRefreshPeerIdGenerator.AccessAwarePeerId;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentPersistentRefreshPeerIdGeneratorTest {

    @Test
    public void shouldHaveOneKeyPerTorrent() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final PeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator(algo, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);

        assertThat(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(1)).generate();
        Mockito.when(algo.generate()).thenReturn("do-not-care2");

        assertThat(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(2)).generate();
        assertThat(generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED))
                .isNotEqualTo(generator.getPeerId(t2, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED));
    }

    @Test
    public void shouldNotRefreshKeyWhenTorrentHasStopped() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final PeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator(algo, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);

        generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STARTED);
        generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED);
        generator.getPeerId(t1, TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE);

        Mockito.verify(algo, Mockito.times(1)).generate();
    }

    @Test
    public void shouldConsiderEntryEvictableIfOlderThanTwoHours() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        final TorrentPersistentRefreshPeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator(algo, false);

        final AccessAwarePeerId oldKey = Mockito.mock(AccessAwarePeerId.class);
        Mockito.when(oldKey.getLastAccess()).thenReturn(LocalDateTime.now().minus(120, ChronoUnit.MINUTES));
        Mockito.when(oldKey.getPeerId()).thenReturn("-BT-C-");
        assertThat(generator.shouldEvictEntry(new AbstractMap.SimpleEntry<>(Mockito.mock(MockedTorrent.class), oldKey))).isTrue();
    }

    @Test
    public void shouldNotConsiderEntryEvictableIfYoungerThanOneHourAndAHalf() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        final TorrentPersistentRefreshPeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator(algo, false);

        final AccessAwarePeerId oldKey = Mockito.mock(AccessAwarePeerId.class);
        Mockito.when(oldKey.getLastAccess()).thenReturn(LocalDateTime.now().minus(89, ChronoUnit.MINUTES));
        Mockito.when(oldKey.getPeerId()).thenReturn("-BT-C-");
        assertThat(generator.shouldEvictEntry(new AbstractMap.SimpleEntry<>(Mockito.mock(MockedTorrent.class), oldKey))).isFalse();
    }

}
