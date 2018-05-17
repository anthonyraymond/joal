package org.araymond.joal.core.ttorrent.client.announcer.tracker;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerMessage;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.slf4j.LoggerFactory.getLogger;

public class TrackerResponseHandler implements ResponseHandler<TrackerMessage> {
    private static final Logger logger = getLogger(TrackerResponseHandler.class);

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
