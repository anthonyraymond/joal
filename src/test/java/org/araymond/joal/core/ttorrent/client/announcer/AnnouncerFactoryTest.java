package org.araymond.joal.core.ttorrent.client.announcer;

import org.apache.http.client.HttpClient;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.NoMoreUriAvailableException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.list;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AnnouncerFactoryTest {

    @Test
    public void shouldCreate() {
        final AnnounceDataAccessor announceDataAccessor = mock(AnnounceDataAccessor.class);
        final AnnouncerFactory announcerFactory = new AnnouncerFactory(announceDataAccessor, Mockito.mock(HttpClient.class), mock(AppConfiguration.class));

        final MockedTorrent torrent = mock(MockedTorrent.class);
        given(torrent.getAnnounceList()).willReturn(list(list(URI.create("http://localhost"))));
        final Announcer announcer = announcerFactory.create(torrent);

        assertThat(announcer.getTorrent()).isEqualTo(torrent);
    }

    @Test
    public void createThrowsIfTorrentContainsNoValidURIs() {
        final AnnounceDataAccessor announceDataAccessor = mock(AnnounceDataAccessor.class);
        final AnnouncerFactory announcerFactory = new AnnouncerFactory(announceDataAccessor, Mockito.mock(HttpClient.class), mock(AppConfiguration.class));

        assertThatThrownBy(() -> announcerFactory.create(mock(MockedTorrent.class)))
                .isInstanceOf(NoMoreUriAvailableException.class);
    }
}
