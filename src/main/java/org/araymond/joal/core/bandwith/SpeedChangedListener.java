package org.araymond.joal.core.bandwith;

import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.util.Map;

public interface SpeedChangedListener {
    void speedsHasChanged(final Map<InfoHash, Speed> speeds);
}
