package org.araymond.joal.core.ttorent.client.announce;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.client.announce.AnnounceResponseListener;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerMessage;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.araymond.joal.core.ttorent.common.protocol.http.HTTPAnnounceRequestMessage;
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

    protected static final Logger logger = LoggerFactory.getLogger(HTTPTrackerClient.class);
    private final BitTorrentClient bitTorrentClient;

    /**
     * Create a new HTTP announcer for the given torrent.
     *
     * @param torrent The torrent we're announcing about.
     * @param peer    Our own peer specification.
     */
    protected HTTPTrackerClient(final TorrentWithStats torrent, final Peer peer, final URI tracker, final BitTorrentClient bitTorrentClient) {
        super(torrent, peer, tracker);

        this.bitTorrentClient = bitTorrentClient;
    }

    /**
     * Build, send and process a tracker announce request.
     * <p>
     * <p>
     * This function first builds an announce request for the specified event
     * with all the required parameters. Then, the request is made to the
     * tracker and the response analyzed.
     * </p>
     * <p>
     * <p>
     * All registered {@link AnnounceResponseListener} objects are then fired
     * with the decoded payload.
     * </p>
     *
     * @param event         The announce event type (can be AnnounceEvent.NONE for
     *                      periodic updates).
     */
    @Override
    public void announce(final AnnounceRequestMessage.RequestEvent event) throws AnnounceException {
        logger.info("Announcing {} to tracker with {}U/{}D/{}L bytes...",
                this.formatAnnounceEvent(event),
                this.torrent.getUploaded(),
                this.torrent.getDownloaded(),
                this.torrent.getLeft());

        final URL target;
        try {
            final HTTPAnnounceRequestMessage request = this.buildAnnounceRequest(event);
            target = request.buildAnnounceURL(this.tracker.toURL(), this.bitTorrentClient);
            logger.debug("Announce url: " + target.toString());
        } catch (final MalformedURLException mue) {
            throw new AnnounceException("Invalid announce URL (" + mue.getMessage() + ")", mue);
        } catch (final MessageValidationException mve) {
            throw new AnnounceException("Announce request creation violated " + "expected protocol (" + mve.getMessage() + ")", mve);
        } catch (final IOException ioe) {
            throw new AnnounceException("Error building announce request (" + ioe.getMessage() + ")", ioe);
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
            logger.trace("Tracker answer was an error: {}", ioe);
        }

        // At this point if the input stream is null it means we have neither a
        // response body nor an error stream from the server. No point in going
        // any further.
        if (in == null) {
            throw new AnnounceException("No response or unreachable tracker!");
        }

        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (!Objects.equals(conn.getHeaderField("Content-Encoding"), "gzip")) {
                outputStream.write(in);
            } else {
                outputStream.write(new GZIPInputStream(in));
            }
            // Parse and handle the response
            final HTTPTrackerMessage message = HTTPTrackerMessage.parse(ByteBuffer.wrap(outputStream.toByteArray()));
            this.handleTrackerAnnounceResponse(message);
        } catch (final IOException ioe) {
            throw new AnnounceException("Error reading tracker response!", ioe);
        } catch (final MessageValidationException mve) {
            throw new AnnounceException("Tracker message violates expected " + "protocol (" + mve.getMessage() + ")", mve);
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
    }

    private void addHttpHeaders(final HttpURLConnection conn) {
        for (final Map.Entry<String, String> header : this.bitTorrentClient.getHeaders()) {
            final String value = header.getValue()
                    .replaceAll("\\{host}", conn.getURL().getHost())
                    .replaceAll("\\{java}", "Java " + System.getProperty("java.version"))
                    .replaceAll("\\{os}", System.getProperty("os.name"));
            conn.addRequestProperty(header.getKey(), value);
        }
    }

    /**
     * Build the announce request tracker message.
     *
     * @param event The announce event (can be <tt>NONE</tt> or <em>null</em>)
     * @return Returns an instance of a {@link HTTPAnnounceRequestMessage}
     * that can be used to generate the fully qualified announce URL, with
     * parameters, to make the announce request.
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws MessageValidationException
     */
    private HTTPAnnounceRequestMessage buildAnnounceRequest(final AnnounceRequestMessage.RequestEvent event) throws IOException,
            MessageValidationException {
        // Build announce request message
        return HTTPAnnounceRequestMessage.craft(
                this.torrent.getTorrent().getInfoHash(),
                this.peer.getPeerId().array(),
                this.peer.getPort(),
                this.torrent.getUploaded(),
                this.torrent.getDownloaded(),
                this.torrent.getLeft(),
                true, false, event,
                this.peer.getIp(),
                this.bitTorrentClient
        );
    }

}
