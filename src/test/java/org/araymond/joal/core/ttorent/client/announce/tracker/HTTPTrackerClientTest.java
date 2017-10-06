package org.araymond.joal.core.ttorent.client.announce.tracker;

import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by raymo on 05/06/2017.
 */
public class HTTPTrackerClientTest {

    @Test
    public void shouldBuild() throws URISyntaxException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");
        final BitTorrentClient client = Mockito.mock(BitTorrentClient.class);

        try {
            new HTTPTrackerClient(torrent, connectionHandler, uri, client);
        } catch (final Throwable t) {
            fail("Should have built.", t);
        }
    }

    @Test
    public void shouldNotBuildWithoutBitTorrentClient() throws URISyntaxException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");
        final BitTorrentClient client = null;

        assertThatThrownBy(() -> new HTTPTrackerClient(torrent, connectionHandler, uri, client))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("BitTorrentClient must not be null.");
    }

}
