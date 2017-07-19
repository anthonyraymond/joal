package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class NeverRefreshKeyGeneratorTest {

    @Test
    public void shouldNeverRefreshKey() {
        final KeyGenerator generator = new NeverRefreshKeyGenerator(8, KeyTypes.HASH, false, false);

        final Set<String> keys = new HashSet<>();
        for (int i = 0; i < 50; ++i) {
            keys.add(generator.getKey(null, RequestEvent.STARTED));
        }

        assertThat(keys).hasSize(1);
    }

}
