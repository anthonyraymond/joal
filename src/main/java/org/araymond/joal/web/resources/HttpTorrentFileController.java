package org.araymond.joal.web.resources;

import org.apache.commons.codec.binary.Base64;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.web.messages.incoming.config.Base64TorrentIncomingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

/**
 * Created by raymo on 12/07/2017.
 */
@ConditionalOnProperty(name = "spring.main.web-environment", havingValue = "true")
@Controller
@RequestMapping("/torrents")
public class HttpTorrentFileController {
    private static final Logger logger = LoggerFactory.getLogger(HttpTorrentFileController.class);

    private final SeedManager seedManager;

    public HttpTorrentFileController(final SeedManager seedManager) {
        this.seedManager = seedManager;
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addTorrent(@RequestBody final Base64TorrentIncomingMessage b64TorrentFile) throws IOException {
        final byte[] bytes = Base64.decodeBase64(b64TorrentFile.getB64String());
        this.seedManager.saveTorrentToDisk(b64TorrentFile.getFileName(), bytes);
    }

}
