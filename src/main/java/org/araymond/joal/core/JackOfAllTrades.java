package org.araymond.joal.core;


import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.araymond.joal.core.client.emulated.EmulatedClientFactory;
import org.araymond.joal.core.config.ConfigProvider;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class JackOfAllTrades {
    private static final Logger logger = LoggerFactory.getLogger(JackOfAllTrades.class);

    private final static Options options = new Options();

    public static void main(final String[] args) throws IOException, NoSuchAlgorithmException, MissingArgumentException, InterruptedException {
        final CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (final ParseException e) {
            throw new IllegalArgumentException(e);
        }


        initConfiguration(getConfigDirPath(cmd).resolve("config.json"));

        final Path emulatedClientFilePath = getConfigDirPath(cmd)
                .resolve("clients")
                .resolve(ConfigProvider.get().getClientFileName());

        final Path torrentFilesPath = getConfigDirPath(cmd);

        final SeedManager seedManager = new SeedManager(
                new TorrentFileProvider(torrentFilesPath.toString()),
                EmulatedClientFactory.createFactory(emulatedClientFilePath)
        );
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Gracefully shutting down application.");
            seedManager.stop();
            logger.info("Gracefully shut down.");

            // Since we disabled log4j2 shutdown hook, we need to handle it manually.
            final LifeCycle context = (LoggerContext) LogManager.getContext(false);
            context.stop();
        }));

        try {
            seedManager.startSeeding();
        } catch (final Throwable t) {
            logger.error("Fatal error encouterd.", t);
            System.exit(1);
        }
    }



    private static void initConfiguration(final Path configFilePath) {
        try {
            ConfigProvider.init(configFilePath);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed to load configuration file: " + configFilePath.toString(), e);
        }
    }

    private static Path getConfigDirPath(final CommandLine cmd) throws MissingArgumentException, FileNotFoundException {
        if (cmd.getArgList().isEmpty()) {
            throw new org.apache.commons.cli.MissingArgumentException("torrent file is required");
        }

        final Path torrentFilePath = Paths.get(cmd.getArgList().get(0));
        if (!Files.exists(torrentFilePath)) {
            throw new FileNotFoundException("Torrent file does not exist: " + torrentFilePath.toAbsolutePath());
        }
        return torrentFilePath;
    }
}
