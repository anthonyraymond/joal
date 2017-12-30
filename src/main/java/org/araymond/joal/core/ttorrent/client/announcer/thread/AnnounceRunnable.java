package org.araymond.joal.core.ttorrent.client.announcer.thread;

import com.google.common.base.Objects;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;

public class AnnounceRunnable implements Runnable {
    private final RequestEvent event;
    private final Announcer announcer;
    private final AnnounceThreadCallback callback;

    AnnounceRunnable(final RequestEvent event, final Announcer announcer, final AnnounceThreadCallback callback) {
        this.event = event;
        this.announcer = announcer;
        this.callback = callback;
    }

    public Announcer getAnnouncer() {
        return announcer;
    }

    @Override
    public void run() {
        try {
            callback.onAnnounceSuccess(event, announcer, announcer.announce(event));
        } catch (final AnnounceException e) {
            callback.onAnnounceFailure(event, announcer, e);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AnnounceRunnable that = (AnnounceRunnable) o;
        return Objects.equal(announcer, that.announcer);
    }

    @Override
    public int hashCode() {
        return announcer.hashCode();
    }
}
