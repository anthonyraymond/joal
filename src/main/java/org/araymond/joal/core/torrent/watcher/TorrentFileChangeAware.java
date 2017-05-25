package org.araymond.joal.core.torrent.watcher;

import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 23/05/2017.
 */
public interface TorrentFileChangeAware {

    void onTorrentAdded(final MockedTorrent torrent);

    void onTorrentRemoved(final MockedTorrent torrent);

}
