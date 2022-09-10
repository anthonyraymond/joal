package org.araymond.joal.core.torrent.watcher;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * Created by raymo on 28/01/2017.
 */
@Slf4j
public class TorrentFileProvider extends FileAlterationListenerAdaptor {

    private final TorrentFileWatcher watcher;
    private final Map<File, MockedTorrent> torrentFiles = Collections.synchronizedMap(new HashMap<>());
    private final Set<TorrentFileChangeAware> torrentFileChangeListener;
    private final Path archiveFolder;

    public TorrentFileProvider(final SeedManager.JoalFoldersPath joalFoldersPath) throws FileNotFoundException {
        Path torrentFolder = joalFoldersPath.getTorrentFilesPath();
        if (!Files.isDirectory(torrentFolder)) {
            // TODO: shouldn't we check&throw in JoalFoldersPath instead?
            log.error("Folder [{}] does not exist", torrentFolder.toAbsolutePath());
            throw new FileNotFoundException(format("Torrent folder [%s] not found", torrentFolder.toAbsolutePath()));
        }

        this.archiveFolder = joalFoldersPath.getTorrentArchivedPath();
        this.watcher = new TorrentFileWatcher(this, torrentFolder);
        this.torrentFileChangeListener = new HashSet<>();
    }

    @VisibleForTesting
    void init() {
        if (!Files.isDirectory(archiveFolder)) {
            if (Files.exists(archiveFolder)) {
                String errMsg = "archive folder exists, but is not a directory";
                log.error(errMsg);
                throw new IllegalStateException(errMsg);
            }

            try {
                Files.createDirectory(archiveFolder);
            } catch (final IOException e) {
                String errMsg = "Failed to create archive folder";
                log.error(errMsg, e);
                throw new IllegalStateException(errMsg, e);
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

    @Override
    public void onFileDelete(final File file) {
        ofNullable(this.torrentFiles.remove(file))
                .ifPresent(removedTorrent -> {
                    log.info("Torrent file deleting detected, hot deleted file [{}]", file.getAbsolutePath());
                    this.torrentFileChangeListener.forEach(listener -> listener.onTorrentFileRemoved(removedTorrent));
                });
    }

    @Override
    public void onFileCreate(final File file) {
        log.info("Torrent file addition detected, hot creating file [{}]", file.getAbsolutePath());
        try {
            final MockedTorrent torrent = MockedTorrent.fromFile(file);
            this.torrentFiles.put(file, torrent);
            this.torrentFileChangeListener.forEach(listener -> listener.onTorrentFileAdded(torrent));
        } catch (final IOException | NoSuchAlgorithmException e) {
            log.warn("Failed to read file [{}], moved to archive folder", file.getAbsolutePath(), e);
            this.moveToArchiveFolder(file);
        } catch (final Exception e) {
            // This thread MUST NOT crash. we need handle any other exception
            log.warn("Unexpected exception was caught for file [{}], moved to archive folder", file.getAbsolutePath(), e);
            this.moveToArchiveFolder(file);
        }
    }

    @Override
    public void onFileChange(final File file) {
        log.info("Torrent file change detected, hot reloading file [{}]", file.getAbsolutePath());
        this.onFileDelete(file);
        this.onFileCreate(file);
    }

    public void registerListener(final TorrentFileChangeAware listener) {
        this.torrentFileChangeListener.add(listener);
    }

    public void unRegisterListener(final TorrentFileChangeAware listener) {
        this.torrentFileChangeListener.remove(listener);
    }

    public MockedTorrent getTorrentNotIn(final List<InfoHash> unwantedTorrents) throws NoMoreTorrentsFileAvailableException {
        Preconditions.checkNotNull(unwantedTorrents, "List of unwantedTorrents cannot be null");

        return this.torrentFiles.values().stream()
                .filter(torrent -> !unwantedTorrents.contains(torrent.getTorrentInfoHash()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .findAny()
                .orElseThrow(() -> new NoMoreTorrentsFileAvailableException("No more torrent file available"));
    }

    void moveToArchiveFolder(final File torrentFile) {
        if (!torrentFile.exists()) {
            return;
        }
        this.onFileDelete(torrentFile);

        try {
            Path moveTarget = archiveFolder.resolve(torrentFile.getName());
            Files.deleteIfExists(moveTarget);
            Files.move(torrentFile.toPath(), moveTarget);
            log.info("Successfully moved file [{}] to archive folder", torrentFile.getAbsolutePath());
        } catch (final IOException e) {
            log.warn("Failed to archive file [{}], the file won't be used anymore for the current session, but it remains in the folder", torrentFile.getAbsolutePath());
        }
    }

    public void moveToArchiveFolder(final InfoHash infoHash) {
        this.torrentFiles.entrySet().stream()
                .filter(entry -> entry.getValue().getTorrentInfoHash().equals(infoHash))
                .map(Map.Entry::getKey)
                .findAny()
                .ifPresentOrElse(this::moveToArchiveFolder,
                        () -> log.warn("Cannot move torrent [{}] to archive folder. Torrent file seems not to be registered in TorrentFileProvider", infoHash));
    }

    public int getTorrentCount() {
        return this.torrentFiles.size();
    }

    public List<MockedTorrent> getTorrentFiles() {
        return new ArrayList<>(this.torrentFiles.values());
    }
}
