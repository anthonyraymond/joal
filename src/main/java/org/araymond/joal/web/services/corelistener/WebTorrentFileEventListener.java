package org.araymond.joal.web.services.corelistener;

import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.events.torrent.files.FailedToAddTorrentFileEvent;
import org.araymond.joal.core.events.torrent.files.TorrentFileAddedEvent;
import org.araymond.joal.core.events.torrent.files.TorrentFileDeletedEvent;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.outgoing.impl.files.FailedToAddTorrentFilePayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileAddedPayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileDeletedPayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by raymo on 11/07/2017.
 */
@ConditionalOnWebUi
@Service
@Slf4j
public class WebTorrentFileEventListener extends WebEventListener {
    @Inject
    public WebTorrentFileEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void torrentFileAdded(final TorrentFileAddedEvent event) {
        log.debug("Send TorrentFileAddedPayload to clients.");

        this.messagingTemplate.convertAndSend("/torrents", new TorrentFileAddedPayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void torrentFileDeleted(final TorrentFileDeletedEvent event) {
        log.debug("Send TorrentFileDeletedPayload to clients.");

        this.messagingTemplate.convertAndSend("/torrents", new TorrentFileDeletedPayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void failedToAddTorrentFile(final FailedToAddTorrentFileEvent event) {
        log.debug("Send FailedToAddTorrentFilePayload to clients.");

        this.messagingTemplate.convertAndSend("/torrents", new FailedToAddTorrentFilePayload(event));
    }

}
