package org.araymond.joal.core.ttorent.client.announce.tracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerMessage;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import static com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage;
import static com.turn.ttorrent.common.protocol.TrackerMessage.MessageValidationException;

/**
 * Created by raymo on 23/01/2017.
 */
public class HTTPTrackerClient extends TrackerClient {

    private static final Logger logger = LoggerFactory.getLogger(HTTPTrackerClient.class);
    private final BitTorrentClient bitTorrentClient;

    /**
     * Create a new HTTP announcer for the given torrent.
     *
     * @param torrent The torrent we're announcing about.
     * @param peer    Our own peer specification.
     */
    public HTTPTrackerClient(final TorrentWithStats torrent, final Peer peer, final URI tracker, final BitTorrentClient bitTorrentClient) {
        super(torrent, peer, tracker);
        Preconditions.checkNotNull(bitTorrentClient, "BitTorrentClient must not be null.");

        this.bitTorrentClient = bitTorrentClient;
    }

    @VisibleForTesting
    void addHttpHeaders(final HttpURLConnection conn) {
        for (final Map.Entry<String, String> header : this.bitTorrentClient.getHeaders()) {
            final String value = header.getValue()
                    .replaceAll("\\{java}", System.getProperty("java.version"))
                    .replaceAll("\\{os}", System.getProperty("os.name"));
            conn.addRequestProperty(header.getKey(), value);
        }
    }

    @Override
    protected HTTPTrackerMessage toTrackerMessage(final ByteBuffer byteBuffer) throws AnnounceException {
        try {
            // Parse and handle the response
            return HTTPTrackerMessage.parse(byteBuffer);
        } catch (final IOException ioe) {
            throw new AnnounceException("Error reading tracker response!", ioe);
        } catch (final MessageValidationException mve) {
            throw new AnnounceException("Tracker message violates expected protocol (" + mve.getMessage() + ")", mve);
        }
    }

    @Override
    protected ByteBuffer makeCallAndGetResponseAsByteBuffer(final AnnounceRequestMessage.RequestEvent event) throws AnnounceException {
        final URL target;
        try {
            target = this.bitTorrentClient.buildAnnounceURL(this.tracker.toURL(), event, this.torrent, this.peer);
            logger.debug("Announce url: " + target.toString());
        } catch (final MalformedURLException mue) {
            throw new AnnounceException("Invalid announce URL (" + mue.getMessage() + ")", mue);
        } catch (final UnsupportedEncodingException e) {
            throw new AnnounceException("Error building announce request (" + e.getMessage() + ")", e);
        }

        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = (HttpURLConnection) target.openConnection();
            this.addHttpHeaders(conn);
            in = conn.getInputStream();
        } catch (final IOException ioe) {
            if (conn != null) {
                in = conn.getErrorStream();
            }
            logger.warn("Tracker answer was an error.", ioe);
        }

        // At this point if the input stream is null it means we have neither a
        // response body nor an error stream from the server. No point in going
        // any further.
        if (in == null) {
            throw new AnnounceException("No response or unreachable tracker!");
        }

        final ByteArrayOutputStream outputStream;
        try {
            outputStream = new ByteArrayOutputStream();
            if (!Objects.equals(conn.getHeaderField("Content-Encoding"), "gzip")) {
                outputStream.write(in);
            } else {
                outputStream.write(new GZIPInputStream(in));
            }
        } catch (final IOException ioe) {
            throw new AnnounceException("Error reading tracker response!", ioe);
        } finally {
            // Make sure we close everything down at the end to avoid resource
            // leaks.
            try {
                in.close();
            } catch (final IOException ioe) {
                logger.warn("Problem ensuring error stream closed!", ioe);
            }

            // This means trying to close the error stream as well.
            final InputStream err = conn.getErrorStream();
            if (err != null) {
                try {
                    err.close();
                } catch (final IOException ioe) {
                    logger.warn("Problem ensuring error stream closed!", ioe);
                }
            }
            conn.disconnect();
        }

        // TODO : ensure outputStream.close() is not required.
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

}
