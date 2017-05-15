package org.araymond.joal.core.torrent.watcher;

import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.utils.TorrentFileCreator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 29/04/2017.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TorrentFileProvider.class})
@TestPropertySource(properties = {
        "joal-conf=src/test/resources/configtest",
})
public class TorrentFileProviderDITest {


    private static final Path resourcePath = Paths.get("src/test/resources/configtest");
    private static final Path torrentsPath = resourcePath.resolve("torrents");

    @BeforeClass
    public static void setUp() throws IOException {
        if (Files.exists(torrentsPath)) {
            Files.walk(torrentsPath, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectory(torrentsPath);
        TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
    }

    @AfterClass
    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    public static void tearDown() throws IOException {
        if (Files.exists(torrentsPath)) {
            Files.walk(torrentsPath, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectory(torrentsPath);
    }

    @Inject
    private TorrentFileProvider torrentFileProvider;

    @Test
    public void shouldInjectConfigProvider() throws NoMoreTorrentsFileAvailableException {
        assertThat(torrentFileProvider.getTorrentCount()).isEqualTo(1);
        assertThat(torrentFileProvider.getRandomTorrentFile().getName()).isEqualTo("ubuntu-17.04-desktop-amd64.iso");
    }

}
