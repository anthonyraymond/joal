package org.araymond.joal.core.ttorrent.client.announcer.exceptions;

import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TooManyAnnouncesFailedInARowExceptionTest {

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = mock(MockedTorrent.class);
        final TooManyAnnouncesFailedInARowException exception = new TooManyAnnouncesFailedInARowException(torrent);

        assertThat(exception.getTorrent()).isEqualTo(torrent);
    }

}
