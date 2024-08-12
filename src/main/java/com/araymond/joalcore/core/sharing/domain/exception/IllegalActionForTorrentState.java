package com.araymond.joalcore.core.sharing.domain.exception;

import java.io.Serial;

public class IllegalActionForTorrentState extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 4725423916873841452L;

    public IllegalActionForTorrentState(String message) {
        super(message);
    }
}
