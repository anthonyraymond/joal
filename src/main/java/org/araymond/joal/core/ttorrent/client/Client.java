package org.araymond.joal.core.ttorrent.client;

import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.torrent.watcher.TorrentFileChangeAware;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Client implements TorrentFileChangeAware {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final AnnounceDataAccessor announceDataAccessor;
    private final List<MockedTorrent> currentlySeedingTorrents;


    public Client(final JoalConfigProvider configProvider, final TorrentFileProvider torrentFileProvider, final AnnounceDataAccessor announceDataAccessor) {
        this.configProvider = configProvider;
        this.torrentFileProvider = torrentFileProvider;
        this.announceDataAccessor = announceDataAccessor;
        this.currentlySeedingTorrents = new ArrayList<>();
    }

    @Override
    public void onTorrentFileAdded(final MockedTorrent torrent) {

    }

    @Override
    public void onTorrentFileRemoved(final MockedTorrent torrent) {

    }
}
