package org.araymond.joal.core.ttorent.client;

import com.google.common.collect.Lists;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.events.NoMoreTorrentsFileAvailableEvent;
import org.araymond.joal.core.events.announce.*;
import org.araymond.joal.core.events.filechange.FailedToAddTorrentFileEvent;
import org.araymond.joal.core.events.filechange.TorrentFileAddedEvent;
import org.araymond.joal.core.events.filechange.TorrentFileDeletedEvent;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.torrent.watcher.TorrentFileChangeAware;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.announce.AnnouncerEventListener;
import org.araymond.joal.core.ttorent.client.bandwidth.BandwidthDispatcher;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by raymo on 14/05/2017.
 */
public class Client implements AnnouncerEventListener, TorrentFileChangeAware {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final ApplicationEventPublisher publisher;
    private final BitTorrentClient bitTorrentClient;
    private final List<Announcer> announcers;
    private final ConnectionHandler self;
    private final BandwidthDispatcher bandwidthDispatcher;
    private ClientState currentState = ClientState.STOPPED;

    public Client(final ConnectionHandler connectionHandler, final JoalConfigProvider configProvider, final TorrentFileProvider torrentFileProvider, final ApplicationEventPublisher publisher, final BitTorrentClient bitTorrentClient) {
        this.configProvider = configProvider;
        this.torrentFileProvider = torrentFileProvider;
        this.publisher = publisher;
        this.bitTorrentClient = bitTorrentClient;
        this.announcers = new ArrayList<>();

        this.self = connectionHandler;
        bandwidthDispatcher = new BandwidthDispatcher(configProvider);
    }

    private void setState(final ClientState state) {
        this.currentState = state;
    }

    public void share() {
        this.torrentFileProvider.registerListener(this);
        this.setState(ClientState.STARTING);
        this.bandwidthDispatcher.start();

        final int numberOfTorrentToSeed = configProvider.get().getSimultaneousSeed();
        for (int i = 0; i < numberOfTorrentToSeed; ++i) {
            try {
                addSeedingTorrent();
            } catch (final NoMoreTorrentsFileAvailableException e) {
                handleNoMoreTorrentToSeed(e);
                break;
            }
        }
        this.setState(ClientState.STARTED);
    }

    private void addSeedingTorrent() throws NoMoreTorrentsFileAvailableException {
        final List<MockedTorrent> unwantedTorrent = this.announcers.stream()
                .map(announcer -> announcer.getSeedingTorrent().getTorrent())
                .collect(Collectors.toList());

        final MockedTorrent torrent = torrentFileProvider.getTorrentNotIn(unwantedTorrent);

        this.addSeedingTorrent(torrent);
    }

    private void addSeedingTorrent(final MockedTorrent torrent) {
        final Announcer announcer = new Announcer(torrent, this.self, this.bitTorrentClient, publisher);
        announcer.registerEventListener(bandwidthDispatcher);
        announcer.registerEventListener(this);
        this.announcers.add(announcer);

        logger.debug("Added announcer for Torrent {}", torrent.getName());
        announcer.start();
    }

    public void stop() {
        this.setState(ClientState.STOPPING);
        this.torrentFileProvider.unRegisterListener(this);
        this.bandwidthDispatcher.stop();

        // We need to work with a copy of the list, because when stop, an event is launched that remove the announcer from the list.
        // And it result in a concurrent modification exception.
        Lists.newArrayList(this.announcers).forEach(Announcer::stop);
        this.setState(ClientState.STOPPED);
    }

    private void handleNoMoreTorrentToSeed(final NoMoreTorrentsFileAvailableException exception) {
        if (this.announcers.isEmpty()) {
            this.publisher.publishEvent(new NoMoreTorrentsFileAvailableEvent());
        }
    }

    @Override
    public void onTorrentFileAdded(final MockedTorrent torrent) {
        this.publisher.publishEvent(new TorrentFileAddedEvent(torrent));

        if (this.currentState != ClientState.STARTED) {
            return;
        }
        if (this.announcers.size() >= configProvider.get().getSimultaneousSeed()) {
            return;
        }
        addSeedingTorrent(torrent);
    }

    @Override
    public void onTorrentFileRemoved(final MockedTorrent torrent) {
        this.publisher.publishEvent(new TorrentFileDeletedEvent(torrent));

        // Work on a copy of the list for concurrency purpose (otherwise manually removing a torrent cause the app to crash)
        for (final Announcer announcer : Lists.newArrayList(this.announcers)) {
            if (announcer.isForTorrent(torrent)) {
                announcer.stop();
            }
        }
    }

    @Override
    public void onInvalidTorrentFile(final String fileName, final String errMessage) {
        // TODO: delete file
        this.publisher.publishEvent(new FailedToAddTorrentFileEvent(fileName, errMessage));
    }

    @Override
    public void onAnnouncerWillAnnounce(final RequestEvent event, final Announcer announcer) {
        publisher.publishEvent(new AnnouncerWillAnnounceEvent(announcer, event));
    }

    @Override
    public void onAnnounceSuccess(final Announcer announcer) {
        publisher.publishEvent(new AnnouncerHasAnnouncedEvent(announcer));
    }

    @Override
    public void onAnnounceFail(final Announcer announcer, final String error) {
        publisher.publishEvent(new AnnouncerHasFailedToAnnounceEvent(announcer, error));
    }

    @Override
    public void onNoMoreLeecherForTorrent(final Announcer announcer, final TorrentWithStats torrent) {
        if (!configProvider.get().shouldKeepTorrentWithZeroLeechers()) {
            this.torrentFileProvider.moveToArchiveFolder(torrent.getTorrent());
            announcer.stop();
        }
    }

    @Override
    public void onShouldDeleteTorrent(final Announcer announcer, final TorrentWithStats torrent) {
        this.torrentFileProvider.moveToArchiveFolder(torrent.getTorrent());
        announcer.stop();
    }

    @Override
    public void onAnnouncerStart(final Announcer announcer) {
        publisher.publishEvent(new AnnouncerHasStartedEvent(announcer));
    }

    @Override
    public void onAnnouncerStop(final Announcer announcer) {
        logger.debug("Removed announcer for Torrent {}", announcer.getSeedingTorrent().getTorrent().getName());
        this.announcers.remove(announcer);

        publisher.publishEvent(new AnnouncerHasStoppedEvent(announcer));

        if (this.currentState != ClientState.STOPPING && this.currentState != ClientState.STOPPED) {
            try {
                addSeedingTorrent();
            } catch (final NoMoreTorrentsFileAvailableException e) {
                handleNoMoreTorrentToSeed(e);
            }
        }
    }

    private enum ClientState {
        STARTING,
        STARTED,
        STOPPING,
        STOPPED
    }

}
