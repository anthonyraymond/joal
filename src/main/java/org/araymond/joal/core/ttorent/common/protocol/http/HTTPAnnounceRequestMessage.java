package org.araymond.joal.core.ttorent.common.protocol.http;


import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerMessage;
import org.araymond.joal.core.client.emulated.BitTorrentClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The announce request message for the HTTP tracker protocol.
 * <p>
 * <p>
 * This class represents the announce request message in the HTTP tracker
 * protocol. It doesn't add any specific fields compared to the generic
 * announce request message, but it provides the means to parse such
 * messages and craft them.
 * </p>
 *
 * @author mpetazzoni
 */
public class HTTPAnnounceRequestMessage extends HTTPTrackerMessage implements AnnounceRequestMessage {

    private final byte[] infoHash;
    private final Peer peer;
    private final long uploaded;
    private final long downloaded;
    private final long left;
    private final boolean compact;
    private final boolean noPeerId;
    private final RequestEvent event;
    private final int numWant;

    private HTTPAnnounceRequestMessage(final ByteBuffer data, final byte[] infoHash, final Peer peer, final long uploaded, final long downloaded, final long left, final boolean compact, final boolean noPeerId, final RequestEvent event, final int numWant) {
        super(Type.ANNOUNCE_REQUEST, data);
        this.infoHash = infoHash;
        this.peer = peer;
        this.downloaded = downloaded;
        this.uploaded = uploaded;
        this.left = left;
        this.compact = compact;
        this.noPeerId = noPeerId;
        this.event = event;
        this.numWant = numWant;
    }

    @Override
    public byte[] getInfoHash() {
        return this.infoHash.clone();
    }

    @Override
    public String getHexInfoHash() {
        return Torrent.byteArrayToHexString(this.infoHash);
    }

    @Override
    public byte[] getPeerId() {
        return this.peer.getPeerId().array();
    }

    @Override
    public String getHexPeerId() {
        return this.peer.getHexPeerId();
    }

    @Override
    public int getPort() {
        return this.peer.getPort();
    }

    @Override
    public long getUploaded() {
        return this.uploaded;
    }

    @Override
    public long getDownloaded() {
        return this.downloaded;
    }

    @Override
    public long getLeft() {
        return this.left;
    }

    @Override
    public boolean getCompact() {
        return this.compact;
    }

    @Override
    public boolean getNoPeerIds() {
        return this.noPeerId;
    }

    @Override
    public RequestEvent getEvent() {
        return this.event;
    }

    @Override
    public String getIp() {
        return this.peer.getIp();
    }

    @Override
    public int getNumWant() {
        return this.numWant;
    }

    /**
     * Build the announce request URL for the given tracker announce URL.
     *
     * @param trackerAnnounceURL The tracker's announce URL.
     * @return The URL object representing the announce request URL.
     */
    public URL buildAnnounceURL(final URL trackerAnnounceURL, final BitTorrentClient bitTorrentClient) throws UnsupportedEncodingException, MalformedURLException {
        final String base = trackerAnnounceURL.toString();
        String emulatedClientQuery = bitTorrentClient.getQuery()
                .replaceAll("\\{infohash}", URLEncoder.encode(new String(this.getInfoHash(), Torrent.BYTE_ENCODING), Torrent.BYTE_ENCODING))
                .replaceAll("\\{peerid}", URLEncoder.encode(new String(this.getPeerId(), Torrent.BYTE_ENCODING), Torrent.BYTE_ENCODING))
                .replaceAll("\\{uploaded}", String.valueOf(this.getUploaded()))
                .replaceAll("\\{downloaded}", String.valueOf(this.getDownloaded()))
                .replaceAll("\\{left}", String.valueOf(this.getLeft()))
                .replaceAll("\\{numwant}", String.valueOf(bitTorrentClient.getNumwant()))
                .replaceAll("\\{port}", String.valueOf(this.getPort()))
                .replaceAll("\\{ip}", this.getIp());


        if (emulatedClientQuery.contains("{key}")) {
            emulatedClientQuery = emulatedClientQuery
                    .replaceAll("\\{key}", URLEncoder.encode(bitTorrentClient.getKey().get(), Torrent.BYTE_ENCODING));
        }
        if (this.getEvent() == null || this.getEvent() == RequestEvent.NONE) {
            // if event was NONE, remove the event from the query string
            emulatedClientQuery = emulatedClientQuery.replaceAll("(event=\\{event})", "");
        } else {
            emulatedClientQuery = emulatedClientQuery.replaceAll("\\{event}", this.getEvent().getEventName());
        }

        final String url = base + (base.contains("?") ? "&" : "?") + emulatedClientQuery;

        return new URL(url);
    }

    public static HTTPAnnounceRequestMessage parse(final ByteBuffer data)
            throws IOException, MessageValidationException {
        final BEValue decoded = BDecoder.bdecode(data);
        if (decoded == null) {
            throw new MessageValidationException(
                    "Could not decode tracker message (not B-encoded?)!");
        }

        final Map<String, BEValue> params = decoded.getMap();

        if (!params.containsKey("info_hash")) {
            throw new MessageValidationException(
                    ErrorMessage.FailureReason.MISSING_HASH.getMessage());
        }

        if (!params.containsKey("peer_id")) {
            throw new MessageValidationException(
                    ErrorMessage.FailureReason.MISSING_PEER_ID.getMessage());
        }

        if (!params.containsKey("port")) {
            throw new MessageValidationException(
                    ErrorMessage.FailureReason.MISSING_PORT.getMessage());
        }

        try {
            final byte[] infoHash = params.get("info_hash").getBytes();
            final byte[] peerId = params.get("peer_id").getBytes();
            final int port = params.get("port").getInt();

            // Default 'uploaded' and 'downloaded' to 0 if the client does
            // not provide it (although it should, according to the spec).
            long uploaded = 0;
            if (params.containsKey("uploaded")) {
                uploaded = params.get("uploaded").getLong();
            }

            long downloaded = 0;
            if (params.containsKey("downloaded")) {
                downloaded = params.get("downloaded").getLong();
            }

            // Default 'left' to -1 to avoid peers entering the COMPLETED
            // state when they don't provide the 'left' parameter.
            long left = -1;
            if (params.containsKey("left")) {
                left = params.get("left").getLong();
            }

            boolean compact = false;
            if (params.containsKey("compact")) {
                compact = params.get("compact").getInt() == 1;
            }

            boolean noPeerId = false;
            if (params.containsKey("no_peer_id")) {
                noPeerId = params.get("no_peer_id").getInt() == 1;
            }

            int numWant = AnnounceRequestMessage.DEFAULT_NUM_WANT;
            if (params.containsKey("numwant")) {
                numWant = params.get("numwant").getInt();
            }

            String ip = null;
            if (params.containsKey("ip")) {
                ip = params.get("ip").getString(Torrent.BYTE_ENCODING);
            }

            RequestEvent event = RequestEvent.NONE;
            if (params.containsKey("event")) {
                event = RequestEvent.getByName(params.get("event")
                        .getString(Torrent.BYTE_ENCODING));
            }

            return new HTTPAnnounceRequestMessage(data, infoHash,
                    new Peer(ip, port, ByteBuffer.wrap(peerId)),
                    uploaded, downloaded, left, compact, noPeerId,
                    event, numWant);
        } catch (final InvalidBEncodingException ibee) {
            throw new MessageValidationException(
                    "Invalid HTTP tracker request!", ibee);
        }
    }

    public static HTTPAnnounceRequestMessage craft(final byte[] infoHash, final byte[] peerId, final int port, final long uploaded, final long downloaded, final long left, final boolean compact, final boolean noPeerId, final RequestEvent event, final String ip, final int numWant) throws IOException, MessageValidationException {
        final Map<String, BEValue> params = new LinkedHashMap<>();
        params.put("info_hash", new BEValue(infoHash));
        params.put("peer_id", new BEValue(peerId));
        params.put("port", new BEValue(port));
        params.put("uploaded", new BEValue(uploaded));
        params.put("downloaded", new BEValue(downloaded));
        params.put("left", new BEValue(left));
        params.put("compact", new BEValue(compact ? 1 : 0));
        params.put("no_peer_id", new BEValue(noPeerId ? 1 : 0));
        params.put("numwant", new BEValue(numWant));

        if (event != null) {
            params.put("event", new BEValue(event.getEventName(), Torrent.BYTE_ENCODING));
        }

        if (ip != null) {
            params.put("ip", new BEValue(ip, Torrent.BYTE_ENCODING));
        }

        return new HTTPAnnounceRequestMessage(
                BEncoder.bencode(params),
                infoHash, new Peer(ip, port, ByteBuffer.wrap(peerId)),
                uploaded, downloaded, left, compact, noPeerId, event, numWant);
    }
}
