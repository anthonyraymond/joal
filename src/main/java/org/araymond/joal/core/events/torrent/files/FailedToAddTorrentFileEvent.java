package org.araymond.joal.core.events.torrent.files;

import com.google.common.base.Preconditions;
import lombok.Getter;

/**
 * Created by raymo on 06/05/2017.
 */
@Getter
public class FailedToAddTorrentFileEvent {
    private final String fileName;
    private final String error;

    public FailedToAddTorrentFileEvent(final String fileName, final String error) {
        Preconditions.checkNotNull(fileName, "File name cannot be null.");
        this.fileName = fileName;
        this.error = error;
    }
}
