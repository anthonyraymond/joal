package org.araymond.joal.core.torrent.watcher;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.isDirectory;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;

/**
 * Hooks up a directory listener to detect torrent file additions,
 * deletions, changes, and notifies provided {@link FileAlterationListener}.
 * <p/>
 * Created by raymo on 01/05/2017.
 */
@Slf4j
class TorrentFileWatcher {
    private static final long DEFAULT_SCAN_INTERVAL_MS = SECONDS.toMillis(5);
    private static final IOFileFilter TORRENT_FILE_FILTER = suffixFileFilter(".torrent");

    private final FileAlterationObserver observer;
    private final FileAlterationListener listener;
    private final File monitoredFolder;
    private final FileAlterationMonitor monitor;

    TorrentFileWatcher(final FileAlterationListener listener, final Path monitoredFolder) {
        this(listener, monitoredFolder, DEFAULT_SCAN_INTERVAL_MS);
    }

    TorrentFileWatcher(final FileAlterationListener listener, final Path monitoredFolder, final long intervalMs) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        Preconditions.checkNotNull(monitoredFolder, "monitoredFolder cannot be null");
        Preconditions.checkArgument(isDirectory(monitoredFolder), "Folder [" + monitoredFolder.toAbsolutePath() + "] does not exists.");
        Preconditions.checkArgument(intervalMs > 0, "intervalMs cannot be less than 1");
        this.listener = listener;

        this.monitoredFolder = monitoredFolder.toFile();
        this.observer = new FileAlterationObserver(this.monitoredFolder, TORRENT_FILE_FILTER);
        this.observer.addListener(this.listener);

        this.monitor = new FileAlterationMonitor(intervalMs);
        this.monitor.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("torrent-file-watcher-%d").build());
        this.monitor.addObserver(this.observer);
    }

    void start() {
        try {
            if (!this.monitoredFolder.exists()) {
                throw new IllegalArgumentException(String.format("Torrent directory [%s] does not exist", monitoredFolder));
            }
            this.monitor.start();
            // Trigger event for already present files:
            FileUtils.listFiles(this.monitoredFolder, TORRENT_FILE_FILTER, null)
                    .forEach(this.listener::onFileCreate);
        } catch (final Exception e) {
            log.error("Failed to start torrent file monitoring", e);
            throw new IllegalStateException("Failed to start torrent file monitoring", e);
        }
    }

    void stop() {
        log.trace("Stopping TorrentFileProvider...");
        this.observer.getListeners().forEach(observer::removeListener);
        try {
            this.monitor.stop(10);
        } catch (final Exception ignored) {
        }

        log.trace("TorrentFileProvider stopped");
    }
}
