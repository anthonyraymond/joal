package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.DelayQueue;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;

@RequiredArgsConstructor
@Getter
public final class AnnounceRequest implements DelayQueue.InfoHashAble {

    private final Announcer announcer;
    private final RequestEvent event;

    public static AnnounceRequest createStart(final Announcer announcer) {
        return new AnnounceRequest(announcer, RequestEvent.STARTED);
    }

    public static AnnounceRequest createRegular(final Announcer announcer) {
        return new AnnounceRequest(announcer, RequestEvent.NONE);
    }

    public static AnnounceRequest createStop(final Announcer announcer) {
        return new AnnounceRequest(announcer, RequestEvent.STOPPED);
    }

    @Override
    public InfoHash getInfoHash() {
        return this.announcer.getTorrentInfoHash();
    }
}
