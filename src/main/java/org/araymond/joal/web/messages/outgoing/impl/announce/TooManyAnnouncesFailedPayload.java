package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.TooManyAnnouncesFailedEvent;

public class TooManyAnnouncesFailedPayload extends AnnouncePayload {
    public TooManyAnnouncesFailedPayload(final TooManyAnnouncesFailedEvent event) {
        super(event.getAnnouncerFacade());
    }
}
