package org.araymond.joal.web.messages.outgoing.impl.files;

import lombok.Getter;
import org.araymond.joal.core.events.torrent.files.FailedToAddTorrentFileEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 10/07/2017.
 */
@Getter
public class FailedToAddTorrentFilePayload implements MessagePayload {
    private final String fileName;
    private final String error;

    public FailedToAddTorrentFilePayload(final FailedToAddTorrentFileEvent event) {
        this.fileName = event.getFileName();
        this.error = event.getError();
    }
}
