package org.araymond.joal.core.client.emulated;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
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
    private final String peerId;
    private final String key;
    private final String query;
    private final List<Map.Entry<String, String>> headers;
    private final Integer numwant;

    BitTorrentClient(final String peerId, final String key, final String query, final Collection<HttpHeader> headers, final Integer numwant) {
        Preconditions.checkArgument(!StringUtils.isBlank(peerId), "peerId cannot be null or empty");
        if (key != null) {
            Preconditions.checkArgument(key.trim().length() != 0, "key can be null but must not be empty");
        }
        Preconditions.checkArgument(!StringUtils.isBlank(query), "query cannot be null or empty");
        Preconditions.checkNotNull(headers, "headers cannot be null");
        Preconditions.checkNotNull(numwant, "numwant cannot be null");
        Preconditions.checkArgument(numwant > 0, "numwant must be greater than 0");
        this.peerId = peerId;
        this.query = query;
        this.headers = headers.stream().map(h -> new AbstractMap.SimpleImmutableEntry<>(h.getName(), h.getValue())).collect(Collectors.toList());
        this.key = key;
        this.numwant = numwant;
    }

    public String getPeerId() {
        return peerId;
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(key);
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

    public URL buildAnnounceURL(final URL trackerAnnounceURI, final RequestEvent event, final TorrentWithStats torrent, final Peer peer) throws UnsupportedEncodingException, MalformedURLException {
        final String base = trackerAnnounceURI.toString();
        String emulatedClientQuery = this.getQuery()
                .replaceAll("\\{infohash}", URLEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING), Torrent.BYTE_ENCODING))
                .replaceAll("\\{peerid}", URLEncoder.encode(this.getPeerId(), Torrent.BYTE_ENCODING))
                .replaceAll("\\{uploaded}", String.valueOf(torrent.getUploaded()))
                .replaceAll("\\{downloaded}", String.valueOf(torrent.getDownloaded()))
                .replaceAll("\\{left}", String.valueOf(torrent.getLeft()))
                .replaceAll("\\{numwant}", String.valueOf(this.getNumwant()))
                .replaceAll("\\{port}", String.valueOf(peer.getPort()))
                .replaceAll("\\{ip}", peer.getIp());

        if (emulatedClientQuery.contains("{key}")) {
            emulatedClientQuery = emulatedClientQuery
                    .replaceAll(
                            "\\{key}",
                            URLEncoder.encode(
                                    this.getKey().orElseThrow(() -> new IllegalStateException("Client request query contains 'key' but BitTorrentClient does not have a key.")),
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
        return com.google.common.base.Objects.equal(peerId, that.peerId) &&
                Objects.equal(query, that.query) &&
                Objects.equal(headers, that.headers) &&
                Objects.equal(numwant, that.numwant);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(peerId, key, query, headers, numwant);
    }

}
