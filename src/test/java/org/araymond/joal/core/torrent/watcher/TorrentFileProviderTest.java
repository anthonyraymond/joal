package org.araymond.joal.core.torrent.watcher;

import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.utils.TorrentFileCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static java.nio.file.Files.exists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 13/03/2017.
 */
public class TorrentFileProviderTest {

    private static final Path resourcePath = Paths.get("src/test/resources/configtest");
    private static final Path torrentsPath = resourcePath.resolve("torrents");
    private static final Path archivedTorrentPath = torrentsPath.resolve("archived");

    private void resetDirectories() throws IOException {
        if (Files.exists(torrentsPath)) {
            Files.walk(torrentsPath, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectory(torrentsPath);
    }

    @After
    @Before
    public void setUpAndTearDown() throws IOException {
        resetDirectories();
    }

    @Test
    public void shouldNotBuildIfFolderDoesNotExists() throws FileNotFoundException {
        assertThatThrownBy(() -> new TorrentFileProvider(resourcePath.resolve("nop").toString(), Mockito.mock(ApplicationEventPublisher.class)))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageStartingWith("Torrent folder '")
                .hasMessageEndingWith("' not found.");
    }

    @Test
    public void shouldCreateArchiveFolderIfNotCreatedAlready() throws IOException {
        Files.deleteIfExists(archivedTorrentPath);
        assertThat(exists(archivedTorrentPath)).isFalse();
        new TorrentFileProvider(resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        assertThat(exists(archivedTorrentPath)).isTrue();
    }

    @Test
    public void shouldFailIfFolderDoesNotContainsTorrentFiles() throws IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));

        assertThatThrownBy(provider::getRandomTorrentFile)
                .isInstanceOf(NoMoreTorrentsFileAvailableException.class)
                .hasMessageContaining("No more torrent file available.");
    }

    @Test
    public void shouldAddFileToListOnCreation() throws IOException {
        TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        assertThat(provider.getTorrentCount()).isEqualTo(0);

        provider.onFileCreate(torrentsPath.resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldNotAddDuplicatedFiles() throws IOException {
        TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        assertThat(provider.getTorrentCount()).isEqualTo(0);

        provider.onFileCreate(torrentsPath.resolve("ubuntu.torrent").toFile());
        provider.onFileCreate(torrentsPath.resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldRemoveFileFromListOnDeletion() throws IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        TorrentFileCreator.create(torrentsPath.resolve("ninja.torrent"), TorrentFileCreator.TorrentType.NINJA_HEAT);

        assertThat(provider.getTorrentCount()).isEqualTo(0);
        provider.onFileCreate(torrentsPath.resolve("ninja.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        provider.onFileDelete(torrentsPath.resolve("ninja.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(0);
    }

    @Test
    public void shouldRemoveThenAddFileToListOnUpdate() throws IOException {
        TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));

        provider.onFileCreate(torrentsPath.resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        provider.onFileChange(torrentsPath.resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldMoveTorrentFileToArchivedFolderFromMockedTorrent() throws IOException, NoMoreTorrentsFileAvailableException {
        TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        provider.onFileCreate(torrentsPath.resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        assertThat(archivedTorrentPath.resolve("ubuntu.torrent")).doesNotExist();
        provider.moveToArchiveFolder(provider.getRandomTorrentFile());
        assertThat(torrentsPath.resolve("ubuntu.torrent")).doesNotExist();

        assertThat(archivedTorrentPath.resolve("ubuntu.torrent")).exists();
    }

    @Test
    public void shouldMoveTorrentFileToArchivedFolder() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        provider.onFileCreate(torrentFile.toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        assertThat(archivedTorrentPath.resolve("ubuntu.torrent")).doesNotExist();
        provider.moveToArchiveFolder(torrentFile.toFile());
        assertThat(torrentsPath.resolve("ubuntu.torrent")).doesNotExist();

        assertThat(archivedTorrentPath.resolve("ubuntu.torrent")).exists();
    }


}
