package org.araymond.joal.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by raymo on 01/05/2017.
 */
public class TorrentFileCreator {
    public static Path create(final Path filePath, final TorrentType type) throws IOException {
        return Files.copy(type.getPath(), filePath);
    }

    public enum TorrentType {
        UBUNTU("ubuntu-17.04-desktop-amd64.iso.torrent"),
        NINJA_HEAT("Ninja_Heat_160.avi.torrent"),
        AUDIO("Audio_20160422_archive.torrent");

        private final String path;

        TorrentType(final String path) {
            this.path = path;
        }

        public Path getPath() {
            return Paths.get("src/test/resources/torrent-store/").resolve(path);
        }
    }

}
