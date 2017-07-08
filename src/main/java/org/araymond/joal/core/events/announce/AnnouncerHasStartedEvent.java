package org.araymond.joal.core.events.announce;

import org.araymond.joal.core.ttorent.client.announce.Announcer;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnouncerHasStartedEvent extends AnnouncerEvent {

    public AnnouncerHasStartedEvent(final Announcer announcer) {
        super(announcer);
    }

}
