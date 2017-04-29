package org.araymond.joal.core.torrent.watcher;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

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
    private static final Path torrentFolderPath = resourcePath.resolve("torrents");

    @BeforeClass
    public static void setUp() throws IOException {
        Files.createDirectories(torrentFolderPath);
    }

    @AfterClass
    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    public static void tearDown() throws IOException {
        Files.walkFileTree(torrentFolderPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Inject
    private TorrentFileProvider torrentFileProvider;

    @Test
    public void shouldInjectConfigProvider() {
        assertThat(torrentFileProvider.getTorrentCount()).isEqualTo(0);
    }

}
