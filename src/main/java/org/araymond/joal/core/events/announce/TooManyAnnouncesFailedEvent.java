package org.araymond.joal.core.events.announce;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;

@RequiredArgsConstructor
@Getter
public class TooManyAnnouncesFailedEvent {
    private final AnnouncerFacade announcerFacade;
}
