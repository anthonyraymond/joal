package org.araymond.joal.core.ttorent.client.announce.tracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.NotImplementedException;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class TrackerClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(TrackerClientProvider.class);

    private final Iterator<URI> addressIterator;
    private final TorrentWithStats torrent;
    private final ConnectionHandler connectionHandler;
    private final BitTorrentClient bitTorrentClient;
    private int addressesCount;

    public TrackerClientProvider(final TorrentWithStats torrent, final ConnectionHandler connectionHandler, final BitTorrentClient bitTorrentClient) {
        this.torrent = torrent;
        this.connectionHandler = connectionHandler;
        this.bitTorrentClient = bitTorrentClient;
        final Set<URI> addresses = torrent.getTorrent().getAnnounceList().stream()
                .unordered()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        this.addressIterator = Iterators.cycle(addresses);
        this.addressesCount = addresses.size();
    }

    public int addressesCount() {
        return this.addressesCount;
    }

    public TrackerClient getNext() {
        final URI nextUri = this.addressIterator.next();

        while (this.addressesCount > 0) {
            try {
                return createTrackerClient(nextUri);
            } catch (final Exception e) {
                logger.warn("Failed to instantiate TrackerClient for " + nextUri.toString() + ", moving to next (in there is more");
                this.addressIterator.remove();
                this.addressesCount--;
            }
        }
        throw new IllegalStateException("No valid trackers for torrent " + torrent.getTorrent().getName());
    }

    @VisibleForTesting
    protected TrackerClient createTrackerClient(final URI tracker) {
        final String scheme = tracker.getScheme();

        if ("http".equals(scheme) || "https".equals(scheme)) {
            return new HTTPTrackerClient(this.torrent, this.connectionHandler, tracker, this.bitTorrentClient);
        } else if ("udp".equals(scheme)) {
            throw new NotImplementedException("UDP Client not implemented yet.");
            //return new UDPTrackerClient(torrent, peer, tracker);
        }

        throw new IllegalStateException("Unsupported announce scheme for torrent " + this.torrent.getTorrent().getName() + ": " + scheme + "!");
    }

}
