package org.araymond.joal.core.torrent.watcher;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by raymo on 28/01/2017.
 */
public class TorrentFileProvider extends FileAlterationListenerAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(TorrentFileProvider.class);

    private final TorrentFileWatcher watcher;
    private final Map<File, MockedTorrent> torrentFiles;
    private final Set<TorrentFileChangeAware> torrentFileChangeListener;
    private final Path torrentFolder;
    private final Path archiveFolder;

    @VisibleForTesting
    void init() {
        if (!Files.exists(archiveFolder)) {
            try {
                Files.createDirectory(archiveFolder);
            } catch (final IOException e) {
                logger.error("Failed to create archive folder.", e);
                throw new IllegalStateException("Failed to create archive folder.", e);
            }
        }
    }

    public void start() {
        this.init();
        this.watcher.start();
    }

    public void stop() {
        this.watcher.stop();
        this.torrentFiles.clear();
    }

    public TorrentFileProvider(final SeedManager.JoalFoldersPath joalFoldersPath) throws FileNotFoundException {
        this.torrentFolder = joalFoldersPath.getTorrentFilesPath();
        if (!Files.exists(torrentFolder)) {
            logger.error("Folder " + torrentFolder.toAbsolutePath() + " does not exists.");
            throw new FileNotFoundException(String.format("Torrent folder '%s' not found.", torrentFolder.toAbsolutePath()));
        }

        this.archiveFolder = joalFoldersPath.getTorrentArchivedPath();
        this.torrentFiles = Collections.synchronizedMap(new HashMap<File, MockedTorrent>());
        this.watcher = new TorrentFileWatcher(this, torrentFolder);
        this.torrentFileChangeListener = new HashSet<>();
    }

    @Override
    public void onFileDelete(final File file) {
        if (!this.torrentFiles.containsKey(file)) {
            return;
        }

        final MockedTorrent torrent = this.torrentFiles.get(file);
        if (torrent == null) {
            return;
        }

        logger.info("Torrent file deleting detected, hot deleted file: {}", file.getAbsolutePath());
        this.torrentFiles.remove(file);
        this.torrentFileChangeListener.forEach(listener -> listener.onTorrentFileRemoved(torrent));
    }

    @Override
    public void onFileCreate(final File file) {
        logger.info("Torrent file addition detected, hot creating file: {}", file.getAbsolutePath());
        try {
            final MockedTorrent torrent = MockedTorrent.fromFile(file);
            this.torrentFiles.put(file, torrent);
            this.torrentFileChangeListener.forEach(listener -> listener.onTorrentFileAdded(torrent));
        } catch (final IOException | NoSuchAlgorithmException e) {
            logger.warn("Failed to read file '{}', moved to archive folder.", file.getAbsolutePath(), e);
            this.moveToArchiveFolder(file);
        } catch (final Exception e) {
            // This thread MUST NOT crash. we need handle any other exception
            logger.warn("Unexpected exception was caught for file '{}', moved to archive folder.", file.getAbsolutePath(), e);
            this.moveToArchiveFolder(file);
        }
    }

    @Override
    public void onFileChange(final File file) {
        logger.info("Torrent file change detected, hot reloading file: {}", file.getAbsolutePath());
        this.onFileDelete(file);
        this.onFileCreate(file);
    }

    public void registerListener(final TorrentFileChangeAware listener) {
        this.torrentFileChangeListener.add(listener);
    }

    public void unRegisterListener(final TorrentFileChangeAware listener) {
        this.torrentFileChangeListener.remove(listener);
    }

    public MockedTorrent getTorrentNotIn(final List<MockedTorrent> unwantedTorrents) throws NoMoreTorrentsFileAvailableException {
        Preconditions.checkNotNull(unwantedTorrents, "List of unwantedTorrents cannot be null.");

        return this.torrentFiles.values().stream()
                .filter(torrent -> !unwantedTorrents.contains(torrent))
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .findFirst()
                .orElseThrow(() -> new NoMoreTorrentsFileAvailableException("No more torrent file available."));
    }

    void moveToArchiveFolder(final File torrentFile) {
        if (!torrentFile.exists()) {
            return;
        }
        this.onFileDelete(torrentFile);

        try {
            Files.deleteIfExists(archiveFolder.resolve(torrentFile.getName()));
            Files.move(torrentFile.toPath(), archiveFolder.resolve(torrentFile.getName()));
            logger.info("Successfully moved file: {} to archive folder", torrentFile.getAbsolutePath());
        } catch (final IOException e) {
            logger.warn("Failed to archive file: {}, the file won't be used anymore for the current session, but it remains on the folder.", e);
        }
    }

    public void moveToArchiveFolder(final String torrentInfoHash) {
        final Optional<File> first = this.torrentFiles.entrySet().stream()
                .filter(entry -> entry.getValue().getHexInfoHash().equals(torrentInfoHash))
                .map(Map.Entry::getKey)
                .findFirst();
        if (first.isPresent()) {
            this.moveToArchiveFolder(first.get());
        } else {
            logger.warn("Cannot find torrent for infohash {}, therefore we can't remove it. Torrent file seems not to be registered in TorrentFileProvider.", torrentInfoHash);
        }
    }

    public void moveToArchiveFolder(final MockedTorrent torrent) {
        final Optional<File> first = this.torrentFiles.entrySet().stream()
                .filter(entry -> entry.getValue().equals(torrent))
                .map(Map.Entry::getKey)
                .findFirst();
        if (first.isPresent()) {
            this.moveToArchiveFolder(first.get());
        } else {
            logger.warn("Cannot move torrent {} to archive folder. Torrent file seems not to be registered in TorrentFileProvider.", torrent.getName());
        }
    }

    public int getTorrentCount() {
        return this.torrentFiles.size();
    }

}
