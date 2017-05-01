package org.araymond.joal.core.torrent.watcher;

import com.google.common.collect.Iterators;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by raymo on 28/01/2017.
 */
@Component
public class TorrentFileProvider extends FileAlterationListenerAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(TorrentFileProvider.class);

    private final TorrentFileWatcher watcher;
    private final Map<File, MockedTorrent> torrentFiles;
    private final Path torrentFolder;
    private final Path archiveFolder;
    private final Random rand;

    @PostConstruct
    void postConstruct() {
        this.watcher.start();
    }

    @PreDestroy
    void preDestroy() {
        this.watcher.stop();
    }

    @Inject
    TorrentFileProvider(@Value("${joal-conf}") final String confFolder) throws FileNotFoundException {
        if (StringUtils.isBlank(confFolder)) {
            throw new IllegalArgumentException("A config path is required.");
        }
        torrentFolder = Paths.get(confFolder).resolve("torrents");
        if (!Files.exists(torrentFolder)) {
            logger.error("Folder " + torrentFolder.toAbsolutePath() + " does not exists.");
            throw new FileNotFoundException(String.format("Torrent folder '%s' not found.", torrentFolder.toAbsolutePath()));
        }

        this.archiveFolder = torrentFolder.resolve("archived");
        if (!Files.exists(archiveFolder)) {
            try {
                Files.createDirectory(archiveFolder);
            } catch (final IOException e) {
                logger.error("Failed to create archive folder.", e);
                throw new IllegalStateException("Failed to create archive folder.", e);
            }
        }

        this.torrentFiles = Collections.synchronizedMap(new HashMap<File, MockedTorrent>());
        this.rand = new Random();

        this.watcher = new TorrentFileWatcher(this, torrentFolder);
    }

    @Override
    public void onFileDelete(final File file) {
        logger.info("Torrent file deleting detected, hot deleted file: {}", file.getAbsolutePath());
        this.torrentFiles.remove(file);
    }

    @Override
    public void onFileCreate(final File file) {
        logger.info("Torrent file addition detected, hot creating file: {}", file.getAbsolutePath());
        try {
            this.torrentFiles.put(file, MockedTorrent.fromFile(file));
        } catch (final IOException | NoSuchAlgorithmException e) {
            logger.warn("Failed to read file '" + file.getAbsolutePath() + "'.", e);
        }
    }

    @Override
    public void onFileChange(final File file) {
        logger.info("Torrent file change detected, hot reloading file: {}", file.getAbsolutePath());
        this.onFileDelete(file);
        this.onFileCreate(file);
    }

    public MockedTorrent getRandomTorrentFile() {
        if (this.torrentFiles.isEmpty()) {
            logger.error("There is no more .torrent file available.");
            throw new IllegalStateException("No more torrent file available.");
        }
        final int indexToPick = this.rand.nextInt(this.torrentFiles.size());
        return Iterators.get(this.torrentFiles.values().iterator(), indexToPick);
    }

    public void moveToArchiveFolder(final File torrentFile) {
        this.onFileDelete(torrentFile);

        if (!torrentFile.exists()) {
            return;
        }

        try {
            Files.move(torrentFile.toPath(), archiveFolder.resolve(torrentFile.getName()));
            logger.info("Successfully moved file: {} to archive folder", torrentFile.getAbsolutePath());
        } catch (final IOException e) {
            logger.warn("Failed to archive file: {}, the file won't be used anymore for the current session, but it remains on the folder.", e);
        }
    }

    public void moveToArchiveFolder(final MockedTorrent torrentFile) {
        final File fileToArchive = this.torrentFiles.entrySet().stream()
                .filter(entry -> entry.getValue().getHexInfoHash().equalsIgnoreCase(torrentFile.getHexInfoHash()))
                .findFirst()
                .orElseThrow(() -> {
                    final IllegalStateException e = new IllegalStateException("Failed to find which torrent file to archive.");
                    logger.warn("Failed to find which torrent file to archive.", e);
                    return e;
                })
                .getKey();
        this.moveToArchiveFolder(fileToArchive);
    }

    public int getTorrentCount() {
        return this.torrentFiles.size();
    }



}
