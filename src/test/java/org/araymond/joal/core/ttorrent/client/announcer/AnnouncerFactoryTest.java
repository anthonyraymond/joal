package org.araymond.joal.core.ttorrent.client.announcer;

import org.apache.http.client.HttpClient;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AnnouncerFactoryTest {

    @Test
    public void shouldCreate() {
        final AnnounceDataAccessor announceDataAccessor = mock(AnnounceDataAccessor.class);
        final AnnouncerFactory announcerFactory = new AnnouncerFactory(announceDataAccessor, Mockito.mock(HttpClient.class));

        final MockedTorrent torrent = mock(MockedTorrent.class);
        final Announcer announcer = announcerFactory.create(torrent);

        assertThat(announcer.getTorrent()).isEqualTo(torrent);
    }

}
