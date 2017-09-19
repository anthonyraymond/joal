package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.AnnouncerHasFailedToAnnounceEvent;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasFailedToAnnouncePayload extends AnnouncePayload {

    private final String error;

    public AnnouncerHasFailedToAnnouncePayload(final AnnouncerHasFailedToAnnounceEvent event) {
        super(event);

        this.error = event.getError();
    }


    public String getError() {
        return error;
    }
}
