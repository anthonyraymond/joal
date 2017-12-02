package org.araymond.joal.core.ttorrent.client;

import org.araymond.joal.core.bandwith.NewBandwidthDispatcher;
import org.araymond.joal.core.bandwith.RandomSpeedProvider;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.SuccessAnnounceResponse;
import org.araymond.joal.core.ttorrent.client.announcer.TorrentAnnounceAware;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.torrent.watcher.TorrentFileChangeAware;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Client implements TorrentAnnounceAware, TorrentFileChangeAware {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final AnnounceQueueConsumer announceQueueConsumer;
    private final AnnounceQueue announceQueue;
    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final BitTorrentClient bitTorrentClient;
    private final ConnectionHandler connectionHandler;
    private final NewBandwidthDispatcher bandwidthDispatcher;
    private boolean isStopping = false;
    private final List<MockedTorrent> currentlySeedingTorrents;


    public Client(final JoalConfigProvider configProvider, final TorrentFileProvider torrentFileProvider, final BitTorrentClient bitTorrentClient, final ConnectionHandler connectionHandler) {
        this.configProvider = configProvider;
        this.torrentFileProvider = torrentFileProvider;
        this.bitTorrentClient = bitTorrentClient;
        this.connectionHandler = connectionHandler;
        this.bandwidthDispatcher = new NewBandwidthDispatcher(3000, new RandomSpeedProvider(configProvider));
        this.currentlySeedingTorrents = new ArrayList<>();
        this.announceQueue = new AnnounceQueue();
        this.announceQueueConsumer = new AnnounceQueueConsumer(announceQueue);
    }

    // TODO: we subscribes to the consumer which is going to dispatch events for announces.

    // TODO: to know how many announcers are running we cant trust the queue, because an element can be announcing (and thus not in the queue anyore)
    // TODO: so we have to keep this counter in Client, and update it with dispatched events.

    private void addSeedingTorrent() {
        try {
            final MockedTorrent torrent = this.torrentFileProvider.getTorrentNotIn(this.currentlySeedingTorrents);
            this.currentlySeedingTorrents.add(torrent);
            this.bandwidthDispatcher.registerTorrent(torrent.getTorrentInfoHash());
            this.announceQueue.addToStart(new Announcer(torrent));
        } catch (final NoMoreTorrentsFileAvailableException ignore) {
        }
    }

    public void start() {

    }

    public void stop() {
        this.isStopping = true;
        // TODO: we better put the queue in STOP state (which means all subseuent add will go into stop queue whatever happens
        // TODO: because we are in a multithread context, and an announcer might be announcing
        this.announceQueue.moveAllToStop();
    }

    @Override
    public void successfullyStarted(final Announcer announcer, final SuccessAnnounceResponse response) {
        this.successfullyAnnounced(announcer, response);
    }

    @Override
    public void successfullyAnnounced(final Announcer announcer, final SuccessAnnounceResponse response) {
        final int leechers = response.getLeechers();
        final int seeders = response.getSeeders();

        this.bandwidthDispatcher.updateTorrentPeers(announcer.getTorrent().getTorrentInfoHash(), seeders, leechers);
        if (!this.configProvider.get().shouldKeepTorrentWithZeroLeechers() && (leechers == 0 || seeders == 0)) {
            this.announceQueue.addToStop(announcer);
        } else {
            this.announceQueue.addToInterval(announcer, response.getInterval());
        }
    }

    @Override
    public void announcedStop(final Announcer announcer) {
        this.bandwidthDispatcher.unregisterTorrent(announcer.getTorrent().getTorrentInfoHash());
        if (this.isStopping) {
            return;
        }
        this.addSeedingTorrent();
    }

    @Override
    public void failedToAnnounce(final Announcer announcer, final String err) {
        this.announceQueue.addToInterval(announcer, announcer.getLastKnownInterval());
    }

    @Override
    public void tooManyFailedAnnouncedInARaw(final Announcer announcer, final int consecutiveFails) {
        this.announceQueue.addToStop(announcer);
    }

    @Override
    public void onTorrentFileAdded(final MockedTorrent torrent) {

    }

    @Override
    public void onTorrentFileRemoved(final MockedTorrent torrent) {

    }
}
