package org.araymond.joal.web.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.bandwith.Speed;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.araymond.joal.core.events.config.ListOfClientFilesEvent;
import org.araymond.joal.core.events.speed.SeedingSpeedsHasChangedEvent;
import org.araymond.joal.core.events.torrent.files.TorrentFileAddedEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.incoming.config.Base64TorrentIncomingMessage;
import org.araymond.joal.web.messages.incoming.config.ConfigIncomingMessage;
import org.araymond.joal.web.messages.outgoing.StompMessage;
import org.araymond.joal.web.messages.outgoing.impl.announce.SuccessfullyAnnouncePayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ConfigHasBeenLoadedPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.InvalidConfigPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ListOfClientFilesPayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileAddedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.state.GlobalSeedStartedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.state.GlobalSeedStoppedPayload;
import org.araymond.joal.web.messages.outgoing.impl.speed.SeedingSpeedHasChangedPayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@ConditionalOnWebUi
@Controller
@Slf4j
public class WebUiController {
    @Autowired
    private ObjectMapper objectMapper;

    private final String pathPrefix;
    private final String host;

    @Inject
    public WebUiController(
            @Value("${joal.ui.proxy.websocket.path:}") final String webSocketPathPrefix,
            @Value("${joal.ui.path.prefix:}") final String uiPathPrefix,
            @Value("${joal.ui.proxy.host:localhost}") final String host
        ) {
        if(StringUtils.isNotBlank(webSocketPathPrefix)) {
            pathPrefix = webSocketPathPrefix;
        } else if (!StringUtils.isBlank(uiPathPrefix)) {
            pathPrefix = uiPathPrefix;
        } else {
            pathPrefix = "";
        }
        this.host = host;
    }

    @GetMapping(value = "/ui/config.js", produces = "application/javascript")
    @ResponseBody
    public ResponseEntity<String> uiConfig() {
        Map<String, Object> result = Map.of("host", host, "pathPrefix", pathPrefix);
        String json = "{}";
        try {
            json = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ignored) {}
        return ResponseEntity.ok(String.format("window.uiConfig = %s;", json));
    }
}
