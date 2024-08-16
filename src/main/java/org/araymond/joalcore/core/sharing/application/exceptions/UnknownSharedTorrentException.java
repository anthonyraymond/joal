package org.araymond.joalcore.core.sharing.application.exceptions;

import java.io.Serial;

public class UnknownSharedTorrentException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1173740473821811898L;

    public UnknownSharedTorrentException(String message) {
        super(message);
    }
}
