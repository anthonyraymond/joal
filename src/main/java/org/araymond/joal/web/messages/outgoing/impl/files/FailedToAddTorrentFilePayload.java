package org.araymond.joal.web.messages.outgoing.impl.files;

import org.araymond.joal.core.events.filechange.FailedToAddTorrentFileEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 10/07/2017.
 */
public class FailedToAddTorrentFilePayload implements MessagePayload {
    private final String fileName;
    private final String error;

    public FailedToAddTorrentFilePayload(final FailedToAddTorrentFileEvent event) {
        this.fileName = event.getFileName();
        this.error = event.getError();
    }

    public String getFileName() {
        return fileName;
    }

    public String getError() {
        return error;
    }
}
