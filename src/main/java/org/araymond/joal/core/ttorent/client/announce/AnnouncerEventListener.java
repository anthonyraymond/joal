package org.araymond.joal.core.ttorent.client.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 14/05/2017.
 */
public interface AnnouncerEventListener {

    void onAnnounceRequesting(final TrackerMessage.AnnounceRequestMessage.RequestEvent event, final long uploaded, final long downloaded, final long left);

    void onNoMoreLeecherForTorrent(final Announcer announcer, final MockedTorrent torrent);

    void onAnnouncerStop(final Announcer announcer, final MockedTorrent torrent);

}
