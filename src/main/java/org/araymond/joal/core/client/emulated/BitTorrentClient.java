package org.araymond.joal.core.client.emulated;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.HttpHeader;

/**
 * Created by raymo on 26/01/2017.
 */
public class BitTorrentClient {
    private final PeerIdGenerator peerIdGenerator;
    private final KeyGenerator keyGenerator;
    private final String query;
    private final List<Map.Entry<String, String>> headers;
    private final Integer numwant;
    private final Integer numwantOnStop;

    BitTorrentClient(final PeerIdGenerator peerIdGenerator, final KeyGenerator keyGenerator, final String query, final Collection<HttpHeader> headers, final Integer numwant, final Integer numwantOnStop) {
        Preconditions.checkNotNull(peerIdGenerator, "peerIdGenerator cannot be null or empty");
        Preconditions.checkArgument(!StringUtils.isBlank(query), "query cannot be null or empty");
        Preconditions.checkNotNull(headers, "headers cannot be null");
        Preconditions.checkNotNull(numwant, "numwant cannot be null");
        Preconditions.checkArgument(numwant > 0, "numwant must be greater than 0");
        Preconditions.checkNotNull(numwantOnStop, "numwantOnStop cannot be null");
        Preconditions.checkArgument(numwantOnStop >= 0, "numwantOnStop must be at least 0");
        this.peerIdGenerator = peerIdGenerator;
        this.query = query;
        this.headers = headers.stream().map(h -> new AbstractMap.SimpleImmutableEntry<>(h.getName(), h.getValue())).collect(Collectors.toList());
        this.keyGenerator = keyGenerator;
        this.numwant = numwant;
        this.numwantOnStop = numwantOnStop;
    }

    public String getPeerId(final MockedTorrent torrent, final RequestEvent event) {
        return peerIdGenerator.getPeerId(torrent, event);
    }

    @VisibleForTesting
    Optional<String> getKey(final MockedTorrent torrent, final RequestEvent event) {
        if (keyGenerator == null) {
            return Optional.empty();
        }
        return Optional.of(keyGenerator.getKey(torrent, event));
    }

    public String getQuery() {
        return query;
    }

    public List<Map.Entry<String, String>> getHeaders() {
        return ImmutableList.copyOf(headers);
    }

    public Integer getNumwant() {
        return numwant;
    }

    public Integer getNumwantOnStop() {
        return numwantOnStop;
    }

    public URL buildAnnounceURL(final URL trackerAnnounceURI, final RequestEvent event, final TorrentWithStats torrent, final Peer peer) throws UnsupportedEncodingException, MalformedURLException {
        final String base = trackerAnnounceURI.toString();
        String emulatedClientQuery = this.getQuery()
                .replaceAll("\\{infohash}", URLEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING), Torrent.BYTE_ENCODING))
                .replaceAll("\\{peerid}", URLEncoder.encode(this.getPeerId(torrent.getTorrent(), event), Torrent.BYTE_ENCODING))
                .replaceAll("\\{uploaded}", String.valueOf(torrent.getUploaded()))
                .replaceAll("\\{downloaded}", String.valueOf(torrent.getDownloaded()))
                .replaceAll("\\{left}", String.valueOf(torrent.getLeft()))
                .replaceAll("\\{numwant}", event == RequestEvent.STOPPED ? String.valueOf(this.getNumwantOnStop()) : String.valueOf(this.getNumwant()))
                .replaceAll("\\{port}", String.valueOf(peer.getPort()))
                .replaceAll("\\{ip}", peer.getIp());

        if (emulatedClientQuery.contains("{key}")) {
            emulatedClientQuery = emulatedClientQuery
                    .replaceAll(
                            "\\{key}",
                            URLEncoder.encode(
                                    getKey(torrent.getTorrent(), event).orElseThrow(() -> new IllegalStateException("Client request query contains 'key' but BitTorrentClient does not have a key.")),
                                    Torrent.BYTE_ENCODING
                            )
                    );
        }
        if (event == null || event == RequestEvent.NONE) {
            // if event was NONE, remove the event from the query string
            emulatedClientQuery = emulatedClientQuery.replaceAll("([&]*event=\\{event})", "");
        } else {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{event}", event.getEventName());
        }

        final String url = base + (base.contains("?") ? "&" : "?") + emulatedClientQuery;

        return new URL(url);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BitTorrentClient that = (BitTorrentClient) o;
        return com.google.common.base.Objects.equal(peerIdGenerator, that.peerIdGenerator) &&
                Objects.equal(keyGenerator, that.keyGenerator) &&
                Objects.equal(query, that.query) &&
                Objects.equal(headers, that.headers) &&
                Objects.equal(numwant, that.numwant) &&
                Objects.equal(numwantOnStop, that.numwantOnStop);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(peerIdGenerator, keyGenerator, query, headers, numwant, numwantOnStop);
    }

}
