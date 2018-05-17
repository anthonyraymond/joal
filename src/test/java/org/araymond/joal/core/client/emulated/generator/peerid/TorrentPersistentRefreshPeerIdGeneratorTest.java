package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.RegexPatternPeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;

import static org.araymond.joal.core.client.emulated.generator.peerid.TorrentPersistentRefreshPeerIdGenerator.AccessAwarePeerId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentPersistentRefreshPeerIdGeneratorTest {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldHaveOneKeyPerTorrent() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care-too-much");
        final PeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator(algo, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 22 })).when(t1).getTorrentInfoHash();
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 42 })).when(t2).getTorrentInfoHash();

        assertThat(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(1)).generate();
        Mockito.when(algo.generate()).thenReturn("!!-not-care-too-much");

        assertThat(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.NONE))
                .isEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(2)).generate();
        assertThat(generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isNotEqualTo(generator.getPeerId(t2.getTorrentInfoHash(), RequestEvent.STARTED));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldNotRefreshKeyWhenTorrentHasStopped() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care-too-much");
        final PeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator(algo, false);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 22 })).when(t1).getTorrentInfoHash();

        generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STARTED);
        generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.STOPPED);
        generator.getPeerId(t1.getTorrentInfoHash(), RequestEvent.NONE);

        Mockito.verify(algo, Mockito.times(1)).generate();
    }

    @Test
    public void shouldConsiderEntryEvictableIfOlderThanTwoHours() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        final TorrentPersistentRefreshPeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator(algo, false);

        final AccessAwarePeerId oldKey = Mockito.mock(AccessAwarePeerId.class);
        Mockito.when(oldKey.getLastAccess()).thenReturn(LocalDateTime.now().minus(120, ChronoUnit.MINUTES));
        Mockito.when(oldKey.getPeerId()).thenReturn("-BT-C-");
        assertThat(generator.shouldEvictEntry(new AbstractMap.SimpleEntry<>(Mockito.mock(InfoHash.class), oldKey))).isTrue();
    }

    @Test
    public void shouldNotConsiderEntryEvictableIfYoungerThanOneHourAndAHalf() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        final TorrentPersistentRefreshPeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator(algo, false);

        final AccessAwarePeerId oldKey = Mockito.mock(AccessAwarePeerId.class);
        Mockito.when(oldKey.getLastAccess()).thenReturn(LocalDateTime.now().minus(89, ChronoUnit.MINUTES));
        Mockito.when(oldKey.getPeerId()).thenReturn("-BT-C-");
        assertThat(generator.shouldEvictEntry(new AbstractMap.SimpleEntry<>(Mockito.mock(InfoHash.class), oldKey))).isFalse();
    }

    @Test
    public void shouldTryToEvictEntryEvery30Calls() {
        final PeerIdAlgorithm algo = new RegexPatternPeerIdAlgorithm("AAAAAAAAAAAAAAAAAAAA");
        final TorrentPersistentRefreshPeerIdGenerator generator = spy(new TorrentPersistentRefreshPeerIdGenerator(algo, false));

        for (int i = 0; i < 30; i++) {
            generator.getPeerId(new InfoHash(("" + i).getBytes()), RequestEvent.STARTED);
        }

        Mockito.verify(generator, times(1)).evictOldEntries();
    }

}
