package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasStartedEventTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnouncerHasStartedEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TorrentWithStats cannot be null");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final AnnouncerHasStartedEvent event = new AnnouncerHasStartedEvent(torrent);

        assertThat(event.getTorrent()).isEqualTo(torrent);
    }

}
