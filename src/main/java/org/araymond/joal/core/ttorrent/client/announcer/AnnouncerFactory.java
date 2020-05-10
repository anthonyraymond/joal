package org.araymond.joal.core.ttorrent.client.announcer;

import org.apache.http.client.HttpClient;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;

public class AnnouncerFactory {
    private final AnnounceDataAccessor announceDataAccessor;
    private final HttpClient httpClient;

    public AnnouncerFactory(final AnnounceDataAccessor announceDataAccessor, HttpClient httpClient) {
        this.announceDataAccessor = announceDataAccessor;
        this.httpClient = httpClient;
    }

    public Announcer create(final MockedTorrent torrent) {
        return new Announcer(torrent, this.announceDataAccessor, httpClient);
    }

}
