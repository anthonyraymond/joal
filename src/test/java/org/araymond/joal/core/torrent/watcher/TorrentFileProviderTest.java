package org.araymond.joal.core.torrent.watcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.exists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 13/03/2017.
 */
public class TorrentFileProviderTest {

    private static final Path resourcePath = Paths.get("src/test/resources/configtest");
    private static final Path torrentFolderPath = resourcePath.resolve("torrents");

    @Before
    public void setUp() throws IOException {
        Files.createDirectories(torrentFolderPath);
    }

    @After
    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    public void tearDown() throws IOException {
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

    private Path addTorrentFile(final String name) throws IOException {
        return Files.createFile(torrentFolderPath.resolve(name));
    }

    @Test
    public void shouldNotBuildIfFolderDoesNotExists() throws FileNotFoundException {
        assertThatThrownBy(() -> new TorrentFileProvider(resourcePath.resolve("torrents").toString()))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageStartingWith("Torrent folder '")
                .hasMessageEndingWith("' not found.");
    }

    @Test
    public void shouldCreateArchiveFolderIfNotCreatedAlready() throws FileNotFoundException {
        new TorrentFileProvider(resourcePath.toString());
        assertThat(exists(torrentFolderPath.resolve("archived"))).isTrue();
    }

    @Test
    public void shouldFailIfFolderDoesNotContainsTorrentFiles() throws IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString());

        assertThatThrownBy(provider::getRandomTorrentFile)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more torrent file available.");
    }

    @Test
    public void shouldMoveTorrentFilesToArchivedFolder() throws IOException {
        final Path file = addTorrentFile("dd.torrent");
        final Path file2 = addTorrentFile("jj.torrent");

        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString());
        provider.moveToArchiveFolder(file.toFile());

        assertThat(provider.getTorrentCount()).isEqualTo(1);
        assertThat(exists(file)).isFalse();
        assertThat(exists(torrentFolderPath.resolve("archived").resolve("dd.torrent"))).isTrue();
        assertThat(exists(file2)).isTrue();
        assertThat(exists(torrentFolderPath.resolve("archived").resolve("jj.torrent"))).isFalse();
    }

    @Test
    public void shouldDetectFileAdditionAndDeletion() throws InterruptedException, IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), 10);

        provider.start();

        final Path filePath = addTorrentFile("qq.torrent");
        Thread.sleep(15);
        assertThat(provider.getRandomTorrentFile().toPath()).isEqualTo(filePath);

        Files.delete(filePath);
        Thread.sleep(15);
        assertThatThrownBy(provider::getRandomTorrentFile)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No more torrent file available.");

        provider.stop();
    }

    @Test
    public void shouldDetectFileAlreadyCreatedOnStartup() throws InterruptedException, IOException {
        addTorrentFile("I have a dream.torrent");
        addTorrentFile("That one day torrent file would be loaded on startup.torrent");
        addTorrentFile("I have a dream today.torrent");

        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), 10);

        provider.start();
        assertThat(provider.getTorrentCount()).isEqualTo(3);
        provider.stop();
    }

    @Test
    public void shouldNotDetectFileInArchivedFolder() throws InterruptedException, IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), 10);

        provider.start();

        final Path filePath = addTorrentFile("w.torrent");
        provider.moveToArchiveFolder(filePath.toFile());
        Thread.sleep(15);
        assertThat(provider.getTorrentCount()).isEqualTo(0);

        provider.stop();
    }

}
