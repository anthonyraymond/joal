package org.araymond.joal.core.events.speed;

import com.google.common.collect.Maps;
import org.araymond.joal.core.bandwith.Speed;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SeedingSpeedsHasChangedEventTest {

    @Test
    public void shouldBuild() {
        final Map<InfoHash, Speed> speeds = Maps.newHashMap();
        final SeedingSpeedsHasChangedEvent event = new SeedingSpeedsHasChangedEvent(speeds);

        assertThat(event.getSpeeds()).isEqualTo(speeds);
    }

}
