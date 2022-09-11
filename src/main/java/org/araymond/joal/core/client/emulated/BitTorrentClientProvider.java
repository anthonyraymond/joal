package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.client.emulated.generator.numwant.NumwantProvider;
import org.araymond.joal.core.config.JoalConfigProvider;

import javax.inject.Provider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.Files.isRegularFile;
import static java.util.stream.Collectors.toList;

/**
 * Provides as with an instance of {@link BitTorrentClient}, based on the
 * configured {@code client}.
 *
 * Created by raymo on 23/04/2017.
 */
@Slf4j
public class BitTorrentClientProvider implements Provider<BitTorrentClient> {
    private BitTorrentClient bitTorrentClient;
    private final JoalConfigProvider configProvider;
    private final ObjectMapper objectMapper;
    private final Path clientsFolderPath;

    public BitTorrentClientProvider(final JoalConfigProvider configProvider, final ObjectMapper objectMapper,
                                    final SeedManager.JoalFoldersPath joalFoldersPath) {
        this.configProvider = configProvider;
        this.objectMapper = objectMapper;
        this.clientsFolderPath = joalFoldersPath.getClientFilesDirPath();
    }

    public List<String> listClientFiles() {
        try (final Stream<Path> paths = Files.walk(this.clientsFolderPath, FOLLOW_LINKS)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.endsWith(".client"))
                    .sorted(new SemanticVersionFilenameComparator())
                    .collect(toList());
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to walk through .clients files in [" + this.clientsFolderPath + "]", e);
        }
    }

    @Override
    public BitTorrentClient get() {
        if (bitTorrentClient == null) {
            try {
                generateNewClient();
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return bitTorrentClient;
    }

    public void generateNewClient() throws FileNotFoundException, IllegalStateException {
        log.debug("Generating new client");
        final Path clientConfigPath = clientsFolderPath.resolve(configProvider.get().getClient());
        if (!isRegularFile(clientConfigPath)) {
            throw new FileNotFoundException(format("BitTorrent client configuration file [%s] not found", clientConfigPath.toAbsolutePath()));
        }

        try {
            BitTorrentClientConfig config = objectMapper.readValue(clientConfigPath.toFile(), BitTorrentClientConfig.class);
            this.bitTorrentClient = createClient(config);
            log.debug("New client successfully generated");
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public BitTorrentClient createClient(BitTorrentClientConfig clientConfig) {
        return new BitTorrentClient(
                clientConfig.getPeerIdGenerator(),
                clientConfig.getKeyGenerator(),
                clientConfig.getUrlEncoder(),
                clientConfig.getQuery(),
                ImmutableList.copyOf(clientConfig.getRequestHeaders()),
                new NumwantProvider(clientConfig.getNumwant(), clientConfig.getNumwantOnStop())
        );
    }

    static final class SemanticVersionFilenameComparator implements Comparator<String> {

        @Override
        public int compare(final String o1, final String o2) {
            // remove file extension and replace '_' (which delimited build number with '.')
            final String o1NameWithoutExtension = FilenameUtils.removeExtension(o1).replaceAll("_", ".");
            final String o2NameWithoutExtension = FilenameUtils.removeExtension(o2).replaceAll("_", ".");

            final String o1ClientName = o1NameWithoutExtension.substring(0, o1NameWithoutExtension.lastIndexOf('-'));
            final String o2ClientName = o2NameWithoutExtension.substring(0, o2NameWithoutExtension.lastIndexOf('-'));

            if (!o1ClientName.equalsIgnoreCase(o2ClientName)) {
                return o1.compareTo(o2);
            }

            final String[] o1VersionSufix = o1NameWithoutExtension
                    .substring(o1NameWithoutExtension.lastIndexOf('-') + 1)
                    .split("\\.");
            final String[] o2VersionSufix = o2NameWithoutExtension
                    .substring(o2NameWithoutExtension.lastIndexOf('-') + 1)
                    .split("\\.");
            final int length = Math.max(o1VersionSufix.length, o2VersionSufix.length);
            for (int i = 0; i < length; i++) {
                final int thisPart = i < o1VersionSufix.length ? Integer.parseInt(o1VersionSufix[i]) : 0;
                final int thatPart = i < o2VersionSufix.length ? Integer.parseInt(o2VersionSufix[i]) : 0;
                if(thisPart < thatPart) return -1;
                if(thisPart > thatPart) return 1;
            }
            return 0;
        }
    }
}
