package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.events.announce.AnnouncerEvent;
import org.araymond.joal.core.ttorent.client.announce.AnnounceResult;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.util.Collection;

/**
 * Created by raymo on 26/06/2017.
 */
public abstract class AnnouncePayload implements MessagePayload {
    private final String id;
    private final String name;
    private final Long size;
    private final Long currentSpeed;
    private final Collection<AnnounceResult> announceHistory;

    protected AnnouncePayload(final AnnouncerEvent event) {
        Preconditions.checkNotNull(event, "AnnouncerEvent must not be null.");

        this.id = event.getTorrent().getTorrent().getHexInfoHash();
        this.name = event.getTorrent().getTorrent().getName();
        this.size = event.getTorrent().getTorrent().getSize();
        this.currentSpeed = event.getTorrent().getCurrentRandomSpeedInBytes();
        this.announceHistory = event.getAnnounceHistory();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public Long getCurrentSpeed() {
        return currentSpeed;
    }

    public Collection<AnnounceResult> getAnnounceHistory() {
        return announceHistory;
    }
}
