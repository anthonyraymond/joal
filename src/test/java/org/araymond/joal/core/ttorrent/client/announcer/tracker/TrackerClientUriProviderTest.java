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

}
