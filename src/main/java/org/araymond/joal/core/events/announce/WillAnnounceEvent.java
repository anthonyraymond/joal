package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;

@RequiredArgsConstructor
@Getter
public class WillAnnounceEvent {
    private final AnnouncerFacade announcerFacade;
    private final RequestEvent event;
}
