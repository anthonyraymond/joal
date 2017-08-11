package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class AlwaysRefreshPeerIdGeneratorTest {

    @Test
    public void shouldRefreshKeyEveryTime() {
        final PeerIdGenerator generator = new AlwaysRefreshPeerIdGenerator("-AA-", "[a-zA-Z0-9]");

        final Set<String> keys = new HashSet<>();
        for (int i = 0; i < 50; ++i) {
            keys.add(generator.getPeerId(null, RequestEvent.STARTED));
        }

        assertThat(keys).hasSize(50);
    }

}
