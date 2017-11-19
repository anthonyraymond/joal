package org.araymond.joal.core.ttorent.client.bandwidth;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

/**
 * Created by raymo on 14/05/2017.
 */
public class TorrentWithStats {

    private final MockedTorrent torrent;
    private Long uploaded;
    private final Long downloaded;
    private final Long left;
    private int leechers;
    private int seeders;
    private int interval = 5;
    private Long currentRandomSpeedInBytes;

    public TorrentWithStats(final MockedTorrent torrent) {
        Preconditions.checkNotNull(torrent, "MockedTorrent cannot be null.");
        this.torrent = torrent;
        this.uploaded = 0L;
        this.downloaded = 0L;
        this.left = 0L;
        this.currentRandomSpeedInBytes = 0L;
    }

    void addUploaded(final Long uploaded) {
        this.uploaded += uploaded;
    }

    void refreshRandomSpeedInBytes(final Long speedInBytes) {
        this.currentRandomSpeedInBytes = speedInBytes;
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

    public int getLeechers() {
        return leechers;
    }

    public void setLeechers(final int leechers) {
        this.leechers = leechers;
    }

    public int getSeeders() {
        return seeders;
    }

    public void setSeeders(final int seeders) {
        this.seeders = seeders;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(final int interval) {
        this.interval = interval;
    }

    public Long getCurrentRandomSpeedInBytes() {
        return currentRandomSpeedInBytes;
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
