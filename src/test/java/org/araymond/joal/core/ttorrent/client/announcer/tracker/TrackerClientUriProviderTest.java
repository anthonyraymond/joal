package org.araymond.joal.core.ttorrent.client.announcer.tracker;

import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TrackerClientUriProviderTest {

    public static TrackerClientUriProvider createOne(final String... uris) {
        final List<URI> uriList = new ArrayList<>();
        for (final String uri : uris) {
            uriList.add(URI.create(uri));
        }
        return new TrackerClientUriProvider(uriList);
    }

}
