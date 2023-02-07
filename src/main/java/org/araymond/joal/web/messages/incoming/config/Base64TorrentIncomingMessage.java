package org.araymond.joal.web.messages.incoming.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Created by raymo on 23/07/2017.
 */
@Getter
public class Base64TorrentIncomingMessage {
    private final String fileName;
    private final String b64String;

    @JsonCreator
    public Base64TorrentIncomingMessage(@JsonProperty("fileName") final String fileName,
                                        @JsonProperty("b64String") final String b64String) {
        this.fileName = fileName;
        this.b64String = b64String;
    }
}
