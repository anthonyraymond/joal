package org.araymond.joal.core.ttorent.client.announce.tracker;

import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TrackerClientProviderTest {

    @Test
    public void shouldRemoveDuplicates() throws URISyntaxException {
        final List<List<URI>> tiers = Arrays.asList(
                Arrays.asList(new URI("http://my.domain.com"), new URI("http://my.duplicate.com"), new URI("http://my.duplicate.com")),
                Arrays.asList(new URI("https://my.domain2.com"), new URI("http://my.duplicate.com"), new URI("http://my.duplicate.com"))
        );

        final TrackerClientProvider trackerClientProvider = new MockedTrackerClientProvider(tiers);

        assertThat(trackerClientProvider.addressesCount()).isEqualTo(3);
    }

    @Test
    public void shouldBeAbleToLoopInfiniteTimes() throws URISyntaxException {
        final List<List<URI>> tiers = Collections.singletonList(
                Arrays.asList(new URI("http://my.domain.com"), new URI("https://my.domain2.com"))
        );

        final TrackerClientProvider trackerClientProvider = new MockedTrackerClientProvider(tiers);

        try {
            IntStream.range(0, 50).forEach((i) -> trackerClientProvider.getNext());
        } catch (final Exception e) {
            fail("should have been able to iterate infinite times", e);
        }
    }

    private static TorrentWithStats torrentForTiers(final Collection<List<URI>> tiers) {
        final MockedTorrent mt = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn(tiers).when(mt).getAnnounceList();
        final TorrentWithStats tws = Mockito.mock(TorrentWithStats.class);
        Mockito.doReturn(mt).when(tws).getTorrent();
        return tws;
    }

    private static class MockedTrackerClientProvider extends TrackerClientProvider {
        MockedTrackerClientProvider(final Collection<List<URI>> tiers) {
            super(torrentForTiers(tiers), null, null);
        }
        @Override
        protected TrackerClient createTrackerClient(final URI tracker) {
            return null;
        }
    }
}
