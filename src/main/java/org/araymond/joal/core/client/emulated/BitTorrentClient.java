package org.araymond.joal.core.client.emulated;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.bandwith.TorrentSeedStats;
import org.araymond.joal.core.client.emulated.generator.UrlEncoder;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.numwant.NumwantProvider;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.exception.UnrecognizedClientPlaceholder;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.ConnectionHandler;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.StandardSystemProperty.JAVA_VERSION;
import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.HttpHeader;

/**
 * Created by raymo on 26/01/2017.
 */
@EqualsAndHashCode(exclude = "urlEncoder")
public class BitTorrentClient {
    private final PeerIdGenerator peerIdGenerator;
    private final KeyGenerator keyGenerator;
    private final UrlEncoder urlEncoder;
    @Getter private final String query;
    @Getter private final Collection<HttpHeader> headers;
    private final NumwantProvider numwantProvider;

    private static final Pattern INFOHASH_PTRN = Pattern.compile("\\{infohash}");
    private static final Pattern UPLOADED_PTRN = Pattern.compile("\\{uploaded}");
    private static final Pattern DOWNLOADED_PTRN = Pattern.compile("\\{downloaded}");
    private static final Pattern LEFT_PTRN = Pattern.compile("\\{left}");
    private static final Pattern PORT_PTRN = Pattern.compile("\\{port}");
    private static final Pattern NUMWANT_PTRN = Pattern.compile("\\{numwant}");
    private static final Pattern PEER_ID_PTRN = Pattern.compile("\\{peerid}");
    private static final Pattern EVENT_PTRN = Pattern.compile("\\{event}");
    private static final Pattern KEY_PTRN = Pattern.compile("\\{key}");
    private static final Pattern JAVA_PTRN = Pattern.compile("\\{java}");
    private static final Pattern OS_PTRN = Pattern.compile("\\{os}");
    private static final Pattern LOCALE_PTRN = Pattern.compile("\\{locale}");
    private static final Pattern AMPERSAND_DUPES_PTRN = Pattern.compile("&{2,}");
    private static final Pattern IP_PTRN = Pattern.compile("\\{ip}");
    private static final Pattern IPV6_PTRN = Pattern.compile("\\{ipv6}");
    private static final Pattern IP_Q_PTRN = Pattern.compile("&*\\w+=\\{ip(?:v6)?}");
    private static final Pattern EVENT_Q_PTRN = Pattern.compile("&*\\w+=\\{event}");
    private static final Pattern PLACEHOLDER_PTRN = Pattern.compile("\\{.*?}");

    BitTorrentClient(final PeerIdGenerator peerIdGenerator, final KeyGenerator keyGenerator, final UrlEncoder urlEncoder,
                     final String query, final Collection<HttpHeader> headers, final NumwantProvider numwantProvider) {
        Preconditions.checkNotNull(peerIdGenerator, "peerIdGenerator cannot be null or empty");
        Preconditions.checkNotNull(urlEncoder, "urlEncoder cannot be null");
        Preconditions.checkArgument(!StringUtils.isBlank(query), "query cannot be null or empty");
        Preconditions.checkNotNull(headers, "headers cannot be null");
        Preconditions.checkNotNull(numwantProvider, "numwantProvider cannot be null");
        this.peerIdGenerator = peerIdGenerator;
        this.urlEncoder = urlEncoder;
        this.query = query;
        this.headers = headers;
        this.keyGenerator = keyGenerator;
        this.numwantProvider = numwantProvider;
    }

    public String getPeerId(final InfoHash infoHash, final RequestEvent event) {
        return peerIdGenerator.getPeerId(infoHash, event);
    }

    @VisibleForTesting
    Optional<String> getKey(final InfoHash infoHash, final RequestEvent event) {
        return ofNullable(keyGenerator)
                .map(keyGen -> keyGen.getKey(infoHash, event));
    }

    @VisibleForTesting
    int getNumwant(final RequestEvent event) {
        return this.numwantProvider.get(event);
    }

    /**
     * For torrent protocol, see https://wiki.theory.org/BitTorrent_Tracker_Protocol
     */
    public String createRequestQuery(final RequestEvent event, final InfoHash torrentInfoHash,
                                     final TorrentSeedStats stats, final ConnectionHandler connectionHandler) {
        String q = INFOHASH_PTRN.matcher(this.getQuery()).replaceAll(urlEncoder.encode(torrentInfoHash.value()));
        q = UPLOADED_PTRN.matcher(q).replaceAll(valueOf(stats.getUploaded()));
        q = DOWNLOADED_PTRN.matcher(q).replaceAll(valueOf(stats.getDownloaded()));
        q = LEFT_PTRN.matcher(q).replaceAll(valueOf(stats.getLeft()));
        q = PORT_PTRN.matcher(q).replaceAll(valueOf(connectionHandler.getPort()));
        q = NUMWANT_PTRN.matcher(q).replaceAll(valueOf(this.getNumwant(event)));

        final String peerId = this.peerIdGenerator.isShouldUrlEncode()
                ? urlEncoder.encode(this.getPeerId(torrentInfoHash, event))
                : this.getPeerId(torrentInfoHash, event);
        q = PEER_ID_PTRN.matcher(q).replaceAll(peerId);

        // set ip or ipv6 then remove placeholders that were left empty
        InetAddress addy = connectionHandler.getIpAddress();
        if (q.contains("{ip}") && addy instanceof Inet4Address) {
            q = IP_PTRN.matcher(q).replaceAll(addy.getHostAddress());
        } else if (q.contains("{ipv6}") && addy instanceof Inet6Address) {
            q = IPV6_PTRN.matcher(q).replaceAll(urlEncoder.encode(addy.getHostAddress()));
        }
        q = IP_Q_PTRN.matcher(q).replaceAll(EMPTY);

        if (event == null || event == RequestEvent.NONE) {
            // if event was NONE, remove the event from the query string; this is the normal announce made at regular intervals.
            q = EVENT_Q_PTRN.matcher(q).replaceAll(EMPTY);
        } else {
            q = EVENT_PTRN.matcher(q).replaceAll(event.getEventName());
        }

        if (q.contains("{key}")) {
            final String key = this.getKey(torrentInfoHash, event)
                    .orElseThrow(() -> new IllegalStateException("Client request query contains 'key' but BitTorrentClient does not have a key"));
            q = KEY_PTRN.matcher(q).replaceAll(urlEncoder.encode(key));
        }

        final Matcher placeholderMatcher = PLACEHOLDER_PTRN.matcher(q);
        if (placeholderMatcher.find()) {
            throw new UnrecognizedClientPlaceholder("Placeholder [" + placeholderMatcher.group() + "] were not recognized while building announce URL");
        }

        q = AMPERSAND_DUPES_PTRN.matcher(q).replaceAll("&");  // collapse dupes

        if (q.endsWith("&")) {
            q = q.substring(0, q.length() - 1);
        }
        if (q.startsWith("&")) {
            q = q.substring(1);
        }

        return q;
    }

    public Set<Map.Entry<String, String>> createRequestHeaders() {
        return this.headers.stream().map(hdr -> {
            String value = JAVA_PTRN.matcher(hdr.getValue()).replaceAll(System.getProperty(JAVA_VERSION.key()));
            value = OS_PTRN.matcher(value).replaceAll(System.getProperty(OS_NAME.key()));
            value = LOCALE_PTRN.matcher(value).replaceAll(Locale.getDefault().toLanguageTag());

            final Matcher placeholderMatcher = PLACEHOLDER_PTRN.matcher(value);
            if (placeholderMatcher.find()) {
                throw new UnrecognizedClientPlaceholder("Placeholder [" + placeholderMatcher.group() + "] were not recognized while building client Headers");
            }

            return new SimpleImmutableEntry<>(hdr.getName(), value);
        }).collect(toSet());
    }
}
