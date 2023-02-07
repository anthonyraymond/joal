package org.araymond.joal.core.ttorrent.client.announcer.tracker;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class TrackerResponseHandler implements ResponseHandler<TrackerMessage> {
    @Override
    public TrackerMessage handleResponse(final HttpResponse response) throws IOException {
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            final String message = "No response from tracker";
            throw new IOException(message, new AnnounceException(message));
        }

        final int contentLength = entity.getContentLength() < 1 ? 1024 : (int) entity.getContentLength();
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(contentLength)) {
            if (response.getStatusLine().getStatusCode() >= 300) {
                log.warn("Tracker response is an error: status {}", response.getStatusLine().getStatusCode());
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
