package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentVolatileRefreshKeyGeneratorTest {

    @Test
    public void shouldHaveOneKeyPerTorrent() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(algo, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 22 })).when(t1).getTorrentInfoHash();
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 42 })).when(t2).getTorrentInfoHash();

        assertThat(generator.getKey(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t1.getTorrentInfoHash(), RequestEvent.NONE))
                .isEqualTo(generator.getKey(t1.getTorrentInfoHash(), RequestEvent.STOPPED));

        assertThat(generator.getKey(t2.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2.getTorrentInfoHash(), RequestEvent.STARTED))
                .isEqualTo(generator.getKey(t2.getTorrentInfoHash(), RequestEvent.NONE))
                .isEqualTo(generator.getKey(t2.getTorrentInfoHash(), RequestEvent.STOPPED));

        Mockito.verify(algo, Mockito.times(2)).generate();
    }

    @Test
    public void shouldRefreshKeyWhenTorrentHasStopped() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(algo, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 22 })).when(t1).getTorrentInfoHash();

        final String key1 = generator.getKey(t1.getTorrentInfoHash(), RequestEvent.STARTED);
        final String key2 = generator.getKey(t1.getTorrentInfoHash(), RequestEvent.STOPPED);

        Mockito.when(algo.generate()).thenReturn("do-not-care2");
        final String key3 = generator.getKey(t1.getTorrentInfoHash(), RequestEvent.STARTED);


        Mockito.verify(algo, Mockito.times(2)).generate();

        assertThat(key1)
                .isEqualTo(key2)
                .isNotEqualTo(key3);
    }

    @Test
    public void shouldNotHaveSameKeyForAllTorrent() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(algo, Casing.NONE);

        final MockedTorrent t1 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 22 })).when(t1).getTorrentInfoHash();
        final MockedTorrent t2 = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(new InfoHash(new byte[] { 42 })).when(t2).getTorrentInfoHash();

        final String key1 = generator.getKey(t1.getTorrentInfoHash(), RequestEvent.STARTED);
        Mockito.when(algo.generate()).thenReturn("do-not-care2");
        final String key2 = generator.getKey(t2.getTorrentInfoHash(), RequestEvent.STARTED);
        assertThat(key1)
                .isNotEqualTo(key2);

        Mockito.verify(algo, Mockito.times(2)).generate();
    }

}
