package org.araymond.joal.web.messages.incoming.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by raymo on 23/07/2017.
 */
public class Base64TorrentIncomingMessage {

    private final String fileName;
    private final String b64String;

    @JsonCreator
    public Base64TorrentIncomingMessage(@JsonProperty("fileName") final String fileName, @JsonProperty("b64String") final String b64String) {
        this.fileName = fileName;
        this.b64String = b64String;
    }

    public String getFileName() {
        return fileName;
    }

    public String getB64String() {
        return b64String;
    }
}
