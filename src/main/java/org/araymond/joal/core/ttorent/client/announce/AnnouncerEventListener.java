package org.araymond.joal.core.ttorent.client.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 14/05/2017.
 */
public interface AnnouncerEventListener {

    void onAnnouncerWillAnnounce(final TrackerMessage.AnnounceRequestMessage.RequestEvent event, final TorrentWithStats torrent);

    void onAnnounceSuccess(final TorrentWithStats torrent, final int interval, final int seeders, final int leechers);

    void onAnnounceFail(final TrackerMessage.AnnounceRequestMessage.RequestEvent event, final TorrentWithStats torrent, final String error);

    void onNoMoreLeecherForTorrent(final Announcer announcer, final TorrentWithStats torrent);

    void onAnnouncerStart(final Announcer announcer, final TorrentWithStats torrent);

    void onAnnouncerStop(final Announcer announcer, final TorrentWithStats torrent);

}
