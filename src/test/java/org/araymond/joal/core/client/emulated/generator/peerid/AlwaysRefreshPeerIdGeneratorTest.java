package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class AlwaysRefreshPeerIdGeneratorTest {

    @Test
    public void shouldRefreshKeyEveryTime() {
        final PeerIdAlgorithm algo = Mockito.mock(PeerIdAlgorithm.class);
        Mockito.when(algo.generate()).thenReturn("do-not-care");
        final PeerIdGenerator generator = new AlwaysRefreshPeerIdGenerator(algo, false);

        for (int i = 0; i < 50; ++i) {
            generator.getPeerId(null, RequestEvent.STARTED);
        }

        Mockito.verify(algo, Mockito.times(50)).generate();
    }

}
