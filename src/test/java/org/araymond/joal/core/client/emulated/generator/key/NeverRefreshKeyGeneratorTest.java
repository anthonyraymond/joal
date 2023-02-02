package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

import static org.mockito.Mockito.times;

/**
 * Created by raymo on 16/07/2017.
 */
public class NeverRefreshKeyGeneratorTest {

    @Test
    public void shouldNeverRefreshKey() {
        final KeyAlgorithm algo = Mockito.mock(KeyAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final KeyGenerator generator = new NeverRefreshKeyGenerator(algo, Casing.NONE);


        for (int i = 0; i < 50; ++i) {
            final InfoHash infoHash = new InfoHash(ByteBuffer.allocate(4).putInt(i).array());
            generator.getKey(infoHash, RequestEvent.STARTED);
        }

        Mockito.verify(algo, times(1)).generate();
    }

}
