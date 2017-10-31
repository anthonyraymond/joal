package org.araymond.joal.core.torrent.watcher;

import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 23/05/2017.
 */
public interface TorrentFileChangeAware {

    void onTorrentFileAdded(final MockedTorrent torrent);

    void onTorrentFileRemoved(final MockedTorrent torrent);

    void onInvalidTorrentFile(final String fileName, final String errMessage);
}
