package org.araymond.joal.torrent.watcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by raymo on 28/01/2017.
 */
public class TorrentFileProvider {
    private static final Logger logger = LoggerFactory.getLogger(TorrentFileProvider.class);
    private static final IOFileFilter torrentFileFilter = FileFilterUtils.suffixFileFilter(".torrent");

    private final FileAlterationObserver observer;
    private final FileAlterationMonitor monitor;

    private final List<File> torrentFiles;
    private final Random rand;

    public TorrentFileProvider(final Path torrentFolder) {
        this(torrentFolder, 30 * 1000);
    }

    public TorrentFileProvider(final Path torrentFolder, final int scanInterval) {
        this.torrentFiles = Collections.synchronizedList(new ArrayList<>());
        this.rand = new Random();

        FileUtils.listFiles(torrentFolder.toFile(), torrentFileFilter, null)
                .forEach(torrentFiles::add);

        observer = new FileAlterationObserver(torrentFolder.toFile(), torrentFileFilter);
        monitor = new FileAlterationMonitor(scanInterval);

        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileChange(final File file) {
                removeFileFromList(file);
                onFileCreate(file);
                logger.info("Torrent file change detected, hot reloaded file: {}", file.getAbsolutePath());
            }

            @Override
            public void onFileCreate(final File file) {
                addFileToList(file);
                logger.info("Torrent file addition detected, hot created file: {}", file.getAbsolutePath());
            }

            @Override
            public void onFileDelete(final File file) {
                removeFileFromList(file);
                logger.info("Torrent file deletion detected, hot deleted file: {}", file.getAbsolutePath());
            }
        });
        monitor.addObserver(observer);
    }

    private void removeFileFromList(final File torrent) {
        this.torrentFiles.removeIf(file -> file.getName().equalsIgnoreCase(torrent.getName()));
    }

    private void addFileToList(final File torrent) {
        this.torrentFiles.add(torrent);
    }

    public File getRandomTorrentFile() {
        if (this.torrentFiles.isEmpty()) {
            logger.error("There is no more .torrent file available.");
            throw new IllegalStateException("No more torrent file available.");
        }
        return this.torrentFiles.get(this.rand.nextInt(this.torrentFiles.size()));
    }

    public void forceRemoveTorrent(final File torrent) {
        this.removeFileFromList(torrent);

        if (!torrent.exists()) {
            return;
        }
        final boolean deleted = torrent.delete();
        if (deleted) {
            logger.info("Successfully deleted file: {}", torrent.getAbsolutePath());
        } else {
            logger.warn("Failed to delete file: {}, the file won't be used anymore for the current session, but it remains on the disk.");
        }
    }

    public void start() {
        try {
            monitor.start();
        } catch (final Exception e) {
            logger.error("Failed to start torrent file monitoring.", e);
        }
    }

    public void stop() {
        logger.trace("Call to stop TorrentFileProvider.");
        this.observer.getListeners()
                .forEach(observer::removeListener);
        try {
            this.monitor.stop(10);
        } catch (final Exception ignored) {
        }
        this.monitor.removeObserver(observer);
        logger.trace("TorrentFileProvider stopped.");
    }
}
