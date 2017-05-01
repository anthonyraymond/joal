package org.araymond.joal.core.torrent.watcher;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by raymo on 01/05/2017.
 */
class TorrentFileWatcher {
    private static final Logger logger = LoggerFactory.getLogger(TorrentFileWatcher.class);
    private static final Integer DEFAULT_SCAN_INTERVAL = 30 * 1000;
    private static final IOFileFilter TORRENT_FILE_FILTER = FileFilterUtils.suffixFileFilter(".torrent");

    private final FileAlterationObserver observer;
    private final FileAlterationListener listener;
    private final File monitoredFolder;
    private final FileAlterationMonitor monitor;

    TorrentFileWatcher(final FileAlterationListener listener, final Path monitoredFolder) {
        this(listener, monitoredFolder, DEFAULT_SCAN_INTERVAL);
    }

    TorrentFileWatcher(final FileAlterationListener listener, final Path monitoredFolder, final Integer interval) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        Preconditions.checkNotNull(monitoredFolder, "monitoredFolder cannot be null");
        Preconditions.checkArgument(Files.exists(monitoredFolder), "Folder '" + monitoredFolder.toAbsolutePath() + "' does not exists.");
        Preconditions.checkNotNull(interval, "interval cannot be null");
        Preconditions.checkArgument(interval > 0, "interval cannot be less than 1");
        this.listener = listener;
        this.monitoredFolder = monitoredFolder.toFile();
        this.monitor = new FileAlterationMonitor(interval);
        this.observer = new FileAlterationObserver(this.monitoredFolder, TORRENT_FILE_FILTER);

        this.observer.addListener(this.listener);
        monitor.addObserver(this.observer);
    }

    void start() {
        try {
            this.monitor.start();
            // Trigger event for already present file
            FileUtils.listFiles(this.monitoredFolder, TorrentFileWatcher.TORRENT_FILE_FILTER, null)
                    .forEach(listener::onFileCreate);
        } catch (final Exception e) {
            logger.error("Failed to start torrent file monitoring.", e);
            throw new IllegalStateException("Failed to start torrent file monitoring.", e);
        }
    }

    void stop() {
        logger.trace("Stopping TorrentFileProvider.");
        this.observer.getListeners().forEach(observer::removeListener);
        try {
            this.monitor.stop(10);
        } catch (final Exception ignored) {
        }
        this.monitor.removeObserver(observer);
        logger.trace("TorrentFileProvider stopped.");
    }

}
