package org.araymond.joal.core.client.emulated;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.araymond.joal.core.client.emulated.generator.UrlEncoder;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.numwant.NumwantProvider;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.exception.UnrecognizedAnnounceParameter;
import org.araymond.joal.core.bandwith.TorrentSeedStats;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URL;
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
    private final UrlEncoder urlEncoder;
    private final String query;
    private final List<Map.Entry<String, String>> headers;
    private final NumwantProvider numwantProvider;

    BitTorrentClient(final PeerIdGenerator peerIdGenerator, final KeyGenerator keyGenerator, final UrlEncoder urlEncoder, final String query, final Collection<HttpHeader> headers, final NumwantProvider numwantProvider) {
        Preconditions.checkNotNull(peerIdGenerator, "peerIdGenerator cannot be null or empty");
        Preconditions.checkNotNull(urlEncoder, "urlEncoder cannot be null");
        Preconditions.checkArgument(!StringUtils.isBlank(query), "query cannot be null or empty");
        Preconditions.checkNotNull(headers, "headers cannot be null");
        Preconditions.checkNotNull(numwantProvider, "numwantProvider cannot be null");
        this.peerIdGenerator = peerIdGenerator;
        this.urlEncoder = urlEncoder;
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

    public String createRequestQuery(final RequestEvent event, final InfoHash torrentInfoHash, final TorrentSeedStats stats, final ConnectionHandler connectionHandler) {
        String emulatedClientQuery = this.getQuery()
                .replaceAll("\\{infohash}", urlEncoder.encode(torrentInfoHash.value()))
                .replaceAll("\\{uploaded}", String.valueOf(stats.getUploaded()))
                .replaceAll("\\{downloaded}", String.valueOf(stats.getDownloaded()))
                .replaceAll("\\{left}", String.valueOf(stats.getLeft()))
                .replaceAll("\\{port}", String.valueOf(connectionHandler.getPort()))
                .replaceAll("\\{numwant}", String.valueOf(this.getNumwant(event)));

        if (this.peerIdGenerator.getShouldUrlEncoded()) {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{peerid}", urlEncoder.encode(this.getPeerId(torrent.getTorrent(), event)));
        } else {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{peerid}", this.getPeerId(torrent.getTorrent(), event));
        }

        // set ip or ipv6 then remove placeholders that were left empty
        if (connectionHandler.getIpAddress() instanceof Inet4Address) {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{ip}", connectionHandler.getIpAddress().getHostAddress());
        } else if(connectionHandler.getIpAddress() instanceof Inet6Address) {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{ipv6}", urlEncoder.encode(connectionHandler.getIpAddress().getHostAddress()));
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
            final String key = this.getKey(torrent.getTorrent(), event).orElseThrow(() -> new IllegalStateException("Client request query contains 'key' but BitTorrentClient does not have a key."));
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{key}", urlEncoder.encode(key));
        }

        final Matcher matcher = Pattern.compile("(\\{.*?})").matcher(emulatedClientQuery);
        if (matcher.find()) {
            final String unrecognizedPlaceHolder = matcher.group();
            throw new UnrecognizedAnnounceParameter("Placeholder " + unrecognizedPlaceHolder + " were not recognized while building announce URL.");
        }
        return emulatedClientQuery;
    }

    public Request buildAnnounceRequest(final URL trackerAnnounceURL, final RequestEvent event, final TorrentWithStats torrent, final ConnectionHandler connectionHandler) throws UnsupportedEncodingException {
        String emulatedClientQuery = this.getQuery()
                .replaceAll("\\{infohash}", urlEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING)))
                .replaceAll("\\{uploaded}", String.valueOf(torrent.getUploaded()))
                .replaceAll("\\{downloaded}", String.valueOf(torrent.getDownloaded()))
                .replaceAll("\\{left}", String.valueOf(torrent.getLeft()))
                .replaceAll("\\{port}", String.valueOf(connectionHandler.getPort()))
                .replaceAll("\\{numwant}", String.valueOf(this.getNumwant(event)));

        if (this.peerIdGenerator.getShouldUrlEncoded()) {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{peerid}", urlEncoder.encode(this.getPeerId(torrent.getTorrent(), event)));
        } else {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{peerid}", this.getPeerId(torrent.getTorrent(), event));
        }

        // set ip or ipv6 then remove placeholders that were left empty
        if (connectionHandler.getIpAddress() instanceof Inet4Address) {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{ip}", connectionHandler.getIpAddress().getHostAddress());
        } else if(connectionHandler.getIpAddress() instanceof Inet6Address) {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{ipv6}", urlEncoder.encode(connectionHandler.getIpAddress().getHostAddress()));
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
            final String key = getKey(torrent.getTorrent(), event).orElseThrow(() -> new IllegalStateException("Client request query contains 'key' but BitTorrentClient does not have a key."));
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{key}", urlEncoder.encode(key));
        }

        final Matcher matcher = Pattern.compile("(\\{.*?})").matcher(emulatedClientQuery);
        if (matcher.find()) {
            final String unrecognizedPlaceHolder = matcher.group();
            throw new UnrecognizedAnnounceParameter("Placeholder " + unrecognizedPlaceHolder + " were not recognized while building announce URL.");
        }

        // Append ? or & only if query contains params
        final String base;
        if (emulatedClientQuery.length() > 0) {
            base = trackerAnnounceURL.toString() + (trackerAnnounceURL.toString().contains("?") ? "&" : "?");
        } else {
            base = trackerAnnounceURL.toString();
        }
        final String url = base + emulatedClientQuery;



        final Request request = Request.Get(url);
        this.addHeadersToRequest(request, trackerAnnounceURL);

        return request;
    }

    public List<Map.Entry<String, String>> createRequestHeaders() {
        final List<Map.Entry<String, String>> headers = new ArrayList<>(this.headers.size() + 1);

        this.headers.stream().forEachOrdered(header -> {
            final String name = header.getKey();
            final String value = header.getValue()
                    .replaceAll("\\{java}", System.getProperty("java.version"))
                    .replaceAll("\\{os}", System.getProperty("os.name"))
                    .replaceAll("\\{locale}", Locale.getDefault().toLanguageTag());

            headers.add(new AbstractMap.SimpleImmutableEntry<>(name, value));
        });
        return headers;
    }

    @VisibleForTesting
    void addHeadersToRequest(final Request request, final URL trackerAnnounceURI) {
        final int port = trackerAnnounceURI.getPort();
        // Add Host header first to prevent Request appending it at the end
        request.addHeader("Host", trackerAnnounceURI.getHost() + (port == -1 ? "" : ":" + port));

        //noinspection SimplifyStreamApiCallChains
        this.headers.stream().forEachOrdered(header -> {
            final String name = header.getKey();
            final String value = header.getValue()
                    .replaceAll("\\{java}", System.getProperty("java.version"))
                    .replaceAll("\\{os}", System.getProperty("os.name"))
                    .replaceAll("\\{locale}", Locale.getDefault().toLanguageTag());

            request.addHeader(name, value);
        });

        // if Connection header was not set, we append it. Apache HttpClient will add it what so ever, so prefer "Close" over "keep-alive"
        final boolean hasConnectionHeader = this.headers.stream()
                .anyMatch(header -> "Connection".equalsIgnoreCase(header.getKey()));
        if (!hasConnectionHeader) {
            request.addHeader("Connection", "Close");
        }
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
