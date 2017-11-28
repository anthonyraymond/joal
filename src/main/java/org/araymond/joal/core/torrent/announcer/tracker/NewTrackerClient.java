package org.araymond.joal.core.torrent.announcer.tracker;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceResponseMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.ErrorMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerMessage;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.araymond.joal.core.torrent.announcer.SuccessAnnounceResponse;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NewTrackerClient {
    private static final Logger logger = LoggerFactory.getLogger(NewTrackerClient.class);

    private final TrackerClientUriProvider trackerClientUriProvider;

    public NewTrackerClient(final MockedTorrent torrent) {
        final Set<URI> trackerURIs = torrent.getAnnounceList().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        this.trackerClientUriProvider = new TrackerClientUriProvider(trackerURIs);
    }

    public final SuccessAnnounceResponse announce(final String requestQuery, final Iterable<Map.Entry<String, String>> headers) throws AnnounceException {
        final URI baseUri;
        try {
            baseUri = this.trackerClientUriProvider.get();
            while (!baseUri.getScheme().startsWith("http")) {
                this.trackerClientUriProvider.deleteCurrentAndMoveToNext();
            }
        } catch (final NoMoreUriAvailableException e) {
            throw new AnnounceException("No more valid tracker URI", e);
        }

        final TrackerMessage responseMessage;
        try {
            responseMessage = this.makeCallAndGetResponseAsByteBuffer(baseUri, requestQuery, headers);
        } catch (final AnnounceException e) {
            // If the request has failed we need to move to the next tracker.
            try {
                this.trackerClientUriProvider.moveToNext();
            } catch (final NoMoreUriAvailableException e1) {
                throw new AnnounceException("No more valid tracker for torrent.", e1);
            }
            throw new AnnounceException(e.getMessage(), e);
        }

        if (responseMessage instanceof ErrorMessage) {
            final ErrorMessage error = (ErrorMessage) responseMessage;
            throw new AnnounceException(error.getReason());
        }

        if (!(responseMessage instanceof AnnounceResponseMessage)) {
            throw new AnnounceException("Unexpected tracker message type " + responseMessage.getType().name() + "!");
        }

        final AnnounceResponseMessage announceResponseMessage = (AnnounceResponseMessage) responseMessage;

        final int interval = announceResponseMessage.getInterval();
        // Subtract one to seeders since we are one of them.
        final int seeders = announceResponseMessage.getComplete() == 0 ? 0 : announceResponseMessage.getComplete() - 1;
        final int leechers = announceResponseMessage.getIncomplete();
        return new SuccessAnnounceResponse(interval, seeders, leechers);
    }

    private TrackerMessage makeCallAndGetResponseAsByteBuffer(final URI announceUri, final String requestQuery, final Iterable<Map.Entry<String, String>> headers) throws AnnounceException {
        final String base = announceUri + (announceUri.toString().contains("?") ? "&": "?");
        final Request request = Request.Get(base + requestQuery);

        String host = announceUri.getHost();
        if (announceUri.getPort() != -1) {
            host += ":" + announceUri.getPort();
        }
        request.addHeader("Host", host);
        headers.forEach(entry -> request.addHeader(entry.getKey(), entry.getValue()));

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
            throw new AnnounceException("Failed to handle tracker response: " + e.getMessage(), e);
        }
    }


    static final class TrackerResponseHandler implements ResponseHandler<TrackerMessage> {
        @Override
        public TrackerMessage handleResponse(final HttpResponse response) throws IOException {
            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                final String message = "No response from tracker";
                throw new IOException(message, new AnnounceException(message));
            }

            final int contentLength = entity.getContentLength() < 1 ? 1024 : (int) entity.getContentLength();
            try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(contentLength)) {
                if (response.getStatusLine().getStatusCode() >= 300) {
                    logger.warn("Tracker response is an error.");
                }

                try {
                    entity.writeTo(outputStream);
                } catch (final IOException e) {
                    final String message = "Failed to read tracker http response";
                    throw new IOException(message, new AnnounceException(message, e));
                }

                try {
                    // Parse and handle the response
                    return HTTPTrackerMessage.parse(ByteBuffer.wrap(outputStream.toByteArray()));
                } catch (final IOException ioe) {
                    final String message = "Error reading tracker response!";
                    throw new IOException(message, new AnnounceException(message, ioe));
                } catch (final TrackerMessage.MessageValidationException mve) {
                    final String message = "Tracker message violates expected protocol (" + mve.getMessage() + ")";
                    throw new IOException(message, new AnnounceException(message, mve));
                }
            }
        }
    }

}
