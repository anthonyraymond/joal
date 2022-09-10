package org.araymond.joal.core.bandwith;

import lombok.Getter;

/**
 * Holds the statistics for given torrent, to be announced/reported back to the tracker.
 *
 * For torrent protocol, see <a href="https://wiki.theory.org/BitTorrent_Tracker_Protocol">BitTorrent Tracker Protocol</a>
 */
@Getter
public class TorrentSeedStats {  // TODO: rename to TorrentSeedState? or TorrentState if/when we also start "downloading"

    /**
     * Total amount uploaded so far
     */
    private long uploaded;
    /**
     * Total amount downloaded so far
     */
    private long downloaded;
    /**
     * bytes this client still hsa to download.
     * Note that this can't be computed from downloaded and the file length since the client might be
     * resuming an earlier download, and there's a chance that some of the downloaded data failed an
     * integrity check and had to be re-downloaded.
     */
    private long left;

    void addUploaded(final long bytes) {
        this.uploaded += bytes;
    }
}
