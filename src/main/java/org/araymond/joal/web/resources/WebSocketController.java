package org.araymond.joal.web.resources;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.codec.binary.Base64;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.bandwith.Speed;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.araymond.joal.core.events.config.ListOfClientFilesEvent;
import org.araymond.joal.core.events.speed.SeedingSpeedsHasChangedEvent;
import org.araymond.joal.core.events.torrent.files.TorrentFileAddedEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by raymo on 28/07/2017.
 */
@ConditionalOnWebUi
@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final SeedManager seedManager;
    private final JoalMessageSendingTemplate messageSendingTemplate;

    @Inject
    public WebSocketController(final SeedManager seedManager, final JoalMessageSendingTemplate messageSendingTemplate) {
        this.seedManager = seedManager;
        this.messageSendingTemplate = messageSendingTemplate;
    }

    /*
    @MessageExceptionHandler
    @SendToUser("/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }
    */

    @MessageMapping("/config/save")
    public void saveNewConf(final ConfigIncomingMessage message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Client ask to save new conf {}", message.toString());
        }

        try {
            seedManager.saveNewConfiguration(message.toAppConfiguration());
        } catch (final Exception e) {
            logger.warn("Failed to save conf {}", message.toString(), e);
            messageSendingTemplate.convertAndSend("/config", new InvalidConfigPayload(e));
        }
    }

    @MessageMapping("/global/start")
    public void startStartSession() throws IOException {
        seedManager.startSeeding();
    }

    @MessageMapping("/global/stop")
    public void stopSeedSession() {
        seedManager.stop();
    }

    @MessageMapping("/torrents/upload")
    public void uploadTorrent(final Base64TorrentIncomingMessage b64TorrentFile) throws IOException {
        final byte[] bytes = Base64.decodeBase64(b64TorrentFile.getB64String());
        this.seedManager.saveTorrentToDisk(b64TorrentFile.getFileName(), bytes);
    }

    @MessageMapping("/torrents/delete")
    public void deleteTorrent(final String torrentInfoHash) {
        this.seedManager.deleteTorrent(torrentInfoHash);
    }

    /**
     * This mapping is bypassing the spring WebSocket broker (because of SubscribeMapping) and send the response
     * directly to the client who subscribed, and only him.
     * <b>READ CAREFULLY</b>: since the response is send ignoring the message broker, client will have to provide the
     * application destination prefix (/joal/events/replay)
     *
     * @return an ordered list of all needed event to rebuild the current application state
     */
    @SubscribeMapping("/initialize-me")
    public List<StompMessage> list() {
        final LinkedList<StompMessage> events = new LinkedList<>();

        // client files list
        events.add(StompMessage.wrap(new ListOfClientFilesPayload(new ListOfClientFilesEvent(this.seedManager.listClientFiles()))));

        // config
        events.addFirst(StompMessage.wrap(new ConfigHasBeenLoadedPayload(new ConfigHasBeenLoadedEvent(this.seedManager.getCurrentConfig()))));

        // torrent files list
        for (final MockedTorrent torrent : this.seedManager.getTorrentFiles()) {
            events.addFirst(StompMessage.wrap(new TorrentFileAddedPayload(new TorrentFileAddedEvent(torrent))));
        }

        // global state
        if (this.seedManager.isSeeding()) {
            events.addFirst(StompMessage.wrap(new GlobalSeedStartedPayload(this.seedManager.getCurrentEmulatedClient())));
        } else {
            events.addFirst(StompMessage.wrap(new GlobalSeedStoppedPayload()));
        }

        // speeds
        final Map<InfoHash, Speed> speedMap = this.seedManager.getSpeedMap();
        if (!speedMap.isEmpty()) {
            events.addFirst(StompMessage.wrap(new SeedingSpeedHasChangedPayload(new SeedingSpeedsHasChangedEvent(speedMap))));
        }

        // Announcers are the most likely to change due to a concurrent access, so we gather them as late as possible, and we put them at the top of the list.
        for (final AnnouncerFacade announcerFacade : this.seedManager.getCurrentlySeedingAnnouncer()) {
            events.addFirst(StompMessage.wrap(new SuccessfullyAnnouncePayload(new SuccessfullyAnnounceEvent(announcerFacade, RequestEvent.STARTED))));
        }
        return events;
    }

}
