package org.araymond.joal.core.client.emulated;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.numwant.NumwantProvider;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.exception.UnrecognizedAnnounceParameter;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final NumwantProvider numwantProvider;

    BitTorrentClient(final PeerIdGenerator peerIdGenerator, final KeyGenerator keyGenerator, final String query, final Collection<HttpHeader> headers, final NumwantProvider numwantProvider) {
        Preconditions.checkNotNull(peerIdGenerator, "peerIdGenerator cannot be null or empty");
        Preconditions.checkArgument(!StringUtils.isBlank(query), "query cannot be null or empty");
        Preconditions.checkNotNull(headers, "headers cannot be null");
        Preconditions.checkNotNull(numwantProvider, "numwantProvider cannot be null");
        this.peerIdGenerator = peerIdGenerator;
        this.query = query;
        this.headers = headers.stream().map(h -> new AbstractMap.SimpleImmutableEntry<>(h.getName(), h.getValue())).collect(Collectors.toList());
        this.keyGenerator = keyGenerator;
        this.numwantProvider = numwantProvider;
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

    @VisibleForTesting
    Integer getNumwant(final RequestEvent event) {
        return this.numwantProvider.get(event);
    }

    public URL buildAnnounceURL(final URL trackerAnnounceURI, final RequestEvent event, final TorrentWithStats torrent, final ConnectionHandler connectionHandler) throws UnsupportedEncodingException, MalformedURLException {
        String emulatedClientQuery = this.getQuery()
                .replaceAll("\\{infohash}", URLEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING), Torrent.BYTE_ENCODING))
                .replaceAll("\\{peerid}", URLEncoder.encode(this.getPeerId(torrent.getTorrent(), event), Torrent.BYTE_ENCODING))
                .replaceAll("\\{uploaded}", String.valueOf(torrent.getUploaded()))
                .replaceAll("\\{downloaded}", String.valueOf(torrent.getDownloaded()))
                .replaceAll("\\{left}", String.valueOf(torrent.getLeft()))
                .replaceAll("\\{port}", String.valueOf(connectionHandler.getPort()))
                .replaceAll("\\{numwant}", String.valueOf(this.getNumwant(event)));

        // set ip or ipv6 then remove placeholders that were left empty
        if (connectionHandler.getIpAddress() instanceof Inet4Address) {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{ip}", connectionHandler.getIpAddress().getHostAddress());
        } else if(connectionHandler.getIpAddress() instanceof Inet6Address) {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{ipv6}", URLEncoder.encode(connectionHandler.getIpAddress().getHostAddress(), Torrent.BYTE_ENCODING));
        }
        emulatedClientQuery = emulatedClientQuery.replaceAll("[&]*[a-zA-Z0-9]+=\\{ipv6}", "");
        emulatedClientQuery = emulatedClientQuery.replaceAll("[&]*[a-zA-Z0-9]+=\\{ip}", "");

        if (event == null || event == RequestEvent.NONE) {
            // if event was NONE, remove the event from the query string
            emulatedClientQuery = emulatedClientQuery.replaceAll("([&]*[a-zA-Z0-9]+=\\{event})", "");
        } else {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{event}", event.getEventName());
        }

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

        final Matcher matcher = Pattern.compile("(\\{.*?})").matcher(emulatedClientQuery);
        if (matcher.find()) {
            final String unrecognizedPlaceHolder = matcher.group();
            throw new UnrecognizedAnnounceParameter("Placeholder " + unrecognizedPlaceHolder + " were not recognized while building announce URL.");
        }

        final String base = trackerAnnounceURI.toString();
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
                Objects.equal(numwantProvider, that.numwantProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(peerIdGenerator, keyGenerator, query, headers, numwantProvider);
    }

}
