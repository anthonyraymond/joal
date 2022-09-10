package org.araymond.joal.core.ttorrent.client.announcer.tracker;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TrackerClientUriProviderTest {

    public static TrackerClientUriProvider createOne(final String... uris) {
        final List<URI> uriList = new ArrayList<>();
        for (final String uri : uris) {
            uriList.add(URI.create(uri));
        }
        return new TrackerClientUriProvider(uriList);
    }

    @Test
    public void shouldInitializeOnGetIfNoMoveToNextWasCalledBefore() {
        final TrackerClientUriProvider provider = createOne("http://localhost");

        assertThat(provider.get().toString()).isEqualTo("http://localhost");
    }

    @Test
    public void shouldMoveToNext() throws NoMoreUriAvailableException {
        final TrackerClientUriProvider provider = createOne("http://localhost", "https://localhost", "http://127.0.0.1", "https://127.0.0.1");

        provider.moveToNext();
        assertThat(provider.get().toString()).isEqualTo("http://localhost");
        provider.moveToNext();
        assertThat(provider.get().toString()).isEqualTo("https://localhost");
        provider.moveToNext();
        assertThat(provider.get().toString()).isEqualTo("http://127.0.0.1");
        provider.moveToNext();
        assertThat(provider.get().toString()).isEqualTo("https://127.0.0.1");
    }

    @Test
    public void shouldDeleteAndMoveToNext() throws NoMoreUriAvailableException {
        final TrackerClientUriProvider provider = createOne("http://localhost", "https://localhost", "http://127.0.0.1", "https://127.0.0.1");

        provider.deleteCurrentAndMoveToNext();
        provider.deleteCurrentAndMoveToNext();
        assertThat(provider.get().toString()).isEqualTo("http://127.0.0.1");
    }

    @Test
    public void shouldFailIfDeleteAndNoMoreAvailable() throws NoMoreUriAvailableException {
        final TrackerClientUriProvider provider = createOne("http://localhost", "https://localhost", "http://127.0.0.1");

        provider.deleteCurrentAndMoveToNext();
        provider.deleteCurrentAndMoveToNext();
        assertThatThrownBy(provider::deleteCurrentAndMoveToNext)
                .isInstanceOf(NoMoreUriAvailableException.class);
    }

    @Test
    public void shouldCycleThroughUriIndefinitely() throws NoMoreUriAvailableException {
        final TrackerClientUriProvider provider = createOne("http://localhost", "https://localhost");

        for (int i = 0; i < 20; i++) {
            provider.moveToNext();
            assertThat(provider.get().toString()).isEqualTo(i % 2 == 0 ? "http://localhost" : "https://localhost");
        }
    }

    @Test
    public void shouldRemoveNonHttpUris() throws NoMoreUriAvailableException {
        final TrackerClientUriProvider uriProvider = createOne(
                "udp://tracker.coppersurfer.tk:80/announce",
                "udp://tracker.coppersurfer.tk:6969/announce",
                "udp://tracker.opentrackr.org:1337/announce",
                "udp://9.rarbg.me:2750/announce",
                "udp://9.rarbg.com:2730/announce",
                "udp://9.rarbg.to:2770/announce",
                "udp://tracker.pirateparty.gr:6969/announce",
                "https://localhost2",
                "udp://public.popcorn-tracker.org:6969/announce",
                "udp://tracker.internetwarriors.net:1337/announce",
                "udp://tracker.vanitycore.co:6969/announce",
                "udp://tracker.zer0day.to:1337/announce",
                "udp://open.stealth.si:80/announce",
                "http://localhost"
        );

        assertThat(uriProvider.get()).isEqualTo(URI.create("https://localhost2"));
        assertThat(uriProvider.get()).isEqualTo(URI.create("https://localhost2"));
        uriProvider.moveToNext();
        assertThat(uriProvider.get()).isEqualTo(URI.create("http://localhost"));
        uriProvider.moveToNext();
        assertThat(uriProvider.get()).isEqualTo(URI.create("https://localhost2"));   // started from the top again
    }

    @Test
    public void shouldThrowExceptionIfNoHTTPUriAreFound() throws Exception {
        assertThatThrownBy(() -> createOne("udp://localhost", "udp://127.0.0.1"))
                .isInstanceOf(NoMoreUriAvailableException.class);
    }
}
