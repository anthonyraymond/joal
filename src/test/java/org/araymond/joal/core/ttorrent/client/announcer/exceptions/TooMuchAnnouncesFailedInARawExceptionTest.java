package org.araymond.joal.core.ttorrent.client.announcer.exceptions;

import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TooMuchAnnouncesFailedInARawExceptionTest {

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = mock(MockedTorrent.class);
        final TooMuchAnnouncesFailedInARawException exception = new TooMuchAnnouncesFailedInARawException(torrent);

        assertThat(exception.getTorrent()).isEqualTo(torrent);
    }

}
