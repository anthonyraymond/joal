package org.araymond.joal.core.events.announce;

import org.araymond.joal.core.ttorent.client.announce.AnnounceResult;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 03/07/2017.
 */
public class AnnouncerEventTest {

    @Test
    public void shouldNotBuildWithoutAnnouncer() {
        assertThatThrownBy(() -> new AnnouncerEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Announcer must not be null");
    }

    @Test
    public void shouldBuild() {
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getAnnounceHistory()).thenReturn(Collections.EMPTY_LIST);
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);

        final AnnouncerEvent announcerEvent = new AnnouncerEvent(announcer);
        assertThat(announcerEvent.getAnnounceHistory()).isEqualTo(Collections.EMPTY_LIST);
        assertThat(announcerEvent.getTorrent()).isEqualTo(torrent);
    }

}
