package org.araymond.joal.core.ttorent.client.bandwidth;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 14/05/2017.
 */
public class TorrentWithStats {

    private final MockedTorrent torrent;
    // TODO : consider switching to BigInteger
    private Long uploaded;
    private final Long downloaded;
    private final Long left;

    public TorrentWithStats(final MockedTorrent torrent) {
        Preconditions.checkNotNull(torrent, "MockedTorrent cannot be null.");
        this.torrent = torrent;
        this.uploaded = 0L;
        this.downloaded = 0L;
        this.left = 0L;
    }

    void addUploaded(final Long uploaded) {
        this.uploaded += uploaded;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }

    /**
     * Get the number of bytes uploaded for this torrent.
     */
    public Long getUploaded() {
        return uploaded;
    }

    /**
     * Get the number of bytes downloaded for this torrent.
     */
    public Long getDownloaded() {
        return downloaded;
    }

    /**
     * Get the number of bytes left to download for this torrent.
     */
    public Long getLeft() {
        return left;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TorrentWithStats that = (TorrentWithStats) o;
        return Objects.equal(torrent, that.torrent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(torrent);
    }
}
