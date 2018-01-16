package org.araymond.joal.core.events.torrent.files;

import com.google.common.base.Preconditions;

/**
 * Created by raymo on 06/05/2017.
 */
public class FailedToAddTorrentFileEvent {
    private final String fileName;
    private final String error;

    public FailedToAddTorrentFileEvent(final String fileName, final String error) {
        Preconditions.checkNotNull(fileName, "File name cannot be null.");
        this.fileName = fileName;
        this.error = error;
    }

    public String getFileName() {
        return fileName;
    }

    public String getError() {
        return error;
    }
}
