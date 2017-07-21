package org.araymond.joal.core.ttorent.client.announce.tracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerMessage;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.exception.UnrecognizedAnnounceParameter;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

import static com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage;
import static com.turn.ttorrent.common.protocol.TrackerMessage.MessageValidationException;

/**
 * Created by raymo on 23/01/2017.
 */
public class HTTPTrackerClient extends TrackerClient {

    private static final Logger logger = LoggerFactory.getLogger(HTTPTrackerClient.class);
    private final BitTorrentClient bitTorrentClient;

    public HTTPTrackerClient(final TorrentWithStats torrent, final ConnectionHandler connectionHandler, final URI tracker, final BitTorrentClient bitTorrentClient) {
        super(torrent, connectionHandler, tracker);
        Preconditions.checkNotNull(bitTorrentClient, "BitTorrentClient must not be null.");

        this.bitTorrentClient = bitTorrentClient;
    }

    @Override
    protected TrackerMessage makeCallAndGetResponseAsByteBuffer(final AnnounceRequestMessage.RequestEvent event) throws AnnounceException {
        final Request request;
        try {
            request = this.bitTorrentClient.buildAnnounceRequest(this.tracker.toURL(), event, this.torrent, this.connectionHandler);
            logger.debug("Announce url: " + request.toString());
        } catch (final MalformedURLException mue) {
            throw new AnnounceException("Invalid announce URL (" + mue.getMessage() + ")", mue);
        } catch (final UnsupportedEncodingException e) {
            throw new AnnounceException("Error building announce request (" + e.getMessage() + ")", e);
        } catch (final UnrecognizedAnnounceParameter e) {
            throw new AnnounceException("Invalid placeholder in client query", e);
        } catch (final Exception remains) {
            throw new AnnounceException("Unhandled exception occurred while building announce URL.");
        }

        final Response response;
        try {
            response = request.execute();
        } catch (final ClientProtocolException e) {
            throw new AnnounceException("Failed to announce: protocol mismatch.", e);
        } catch (final IOException e) {
            throw new AnnounceException("Failed to announce: error or connection aborted.", e);
        }

        try {
            return response.handleResponse(new TrackerResponseHandler());
        } catch (final IOException e) {
            throw new AnnounceException("Failed to handle tracker response", e);
        }
    }

    static final class TrackerResponseHandler implements ResponseHandler<TrackerMessage> {
        @Override
        public TrackerMessage handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException(new AnnounceException("No response from tracker"));
            }

            final int contentLength = entity.getContentLength() < 1 ? 1024 : (int) entity.getContentLength();
            try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(contentLength)) {
                if (response.getStatusLine().getStatusCode() >= 300) {
                    logger.warn("Tracker response is an error.");
                }

                try {
                    entity.writeTo(outputStream);
                } catch (final IOException e) {
                    throw new IOException(new AnnounceException("Failed to read tracker http response", e));
                }

                try {
                    // Parse and handle the response
                    return HTTPTrackerMessage.parse(ByteBuffer.wrap(outputStream.toByteArray()));
                } catch (final IOException ioe) {
                    throw new IOException(new AnnounceException("Error reading tracker response!", ioe));
                } catch (final MessageValidationException mve) {
                    throw new IOException(new AnnounceException("Tracker message violates expected protocol (" + mve.getMessage() + ")", mve));
                }
            }
        }
    }
}
