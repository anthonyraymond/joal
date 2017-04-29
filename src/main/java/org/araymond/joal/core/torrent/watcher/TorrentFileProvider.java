package org.araymond.joal.core.torrent.watcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by raymo on 28/01/2017.
 */
@Component
public class TorrentFileProvider {
    private static final Logger logger = LoggerFactory.getLogger(TorrentFileProvider.class);
    private static final IOFileFilter torrentFileFilter = FileFilterUtils.suffixFileFilter(".torrent");

    private final FileAlterationObserver observer;
    private final FileAlterationMonitor monitor;

    private final List<File> torrentFiles;
    private final Path archiveFolder;
    private final Random rand;

    @Inject
    public TorrentFileProvider(@Value("${joal-conf}") final String confFolder) throws FileNotFoundException {
        this(confFolder, 30 * 1000);
    }

    TorrentFileProvider(@Value("${joal-conf}") final String confFolder, final int scanInterval) throws FileNotFoundException {
        if (StringUtils.isBlank(confFolder)) {
            throw new IllegalArgumentException("A config path is required.");
        }
        final Path torrentFolder = Paths.get(confFolder).resolve("torrents");
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
                addFileToList(file);
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

    public void moveToArchiveFolder(final File torrentFile) {
        this.removeFileFromList(torrentFile);

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

    public int getTorrentCount() {
        return this.torrentFiles.size();
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
        this.observer.getListeners().forEach(observer::removeListener);
        try {
            this.monitor.stop(10);
        } catch (final Exception ignored) {
        }
        this.monitor.removeObserver(observer);
        logger.trace("TorrentFileProvider stopped.");
    }


}
