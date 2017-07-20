package org.araymond.joal.web.resources;

import com.google.common.base.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

/**
 * Created by raymo on 12/07/2017.
 */
@Controller
@RequestMapping("/torrents")
public class HttpTorrentFileController {
    private static final Logger logger = LoggerFactory.getLogger(HttpTorrentFileController.class);

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public void addTorrent(@RequestBody final String b64TorrentFile) throws IOException {
        try {
            final byte[] bytes = Base64.decodeBase64(b64TorrentFile);
            logger.info(new String(bytes, Charsets.ISO_8859_1));
        } catch (final Exception e) {
            logger.error("failed to add file", e);
        }
    }

}
