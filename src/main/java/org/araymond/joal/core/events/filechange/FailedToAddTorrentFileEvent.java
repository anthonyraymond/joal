package org.araymond.joal.core.events.filechange;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

import java.io.File;

/**
 * Created by raymo on 06/05/2017.
 */
public class FailedToAddTorrentFileEvent {
    private final File file;
    private final String error;

    public FailedToAddTorrentFileEvent(final File file, final String error) {
        Preconditions.checkNotNull(file, "File cannot be null.");
        this.file = file;
        this.error = error;
    }

    public File getFile() {
        return file;
    }

    public String getError() {
        return error;
    }
}
