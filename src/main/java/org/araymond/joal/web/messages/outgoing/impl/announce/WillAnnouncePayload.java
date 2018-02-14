package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.WillAnnounceEvent;

public class WillAnnouncePayload extends AnnouncePayload {
    public WillAnnouncePayload(final WillAnnounceEvent event) {
        super(event.getAnnouncerFacade());
    }
}
