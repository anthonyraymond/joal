package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.TooManyAnnouncesFailedEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

public class TooManyAnnouncesFailedPayload extends AnnouncePayload {
    public TooManyAnnouncesFailedPayload(final TooManyAnnouncesFailedEvent event) {
        super(event.getAnnouncerFacade());
    }
}
