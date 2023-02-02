package org.araymond.joal.core.torrent.watcher;

import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.utils.TorrentFileCreator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;

import static java.nio.file.Files.exists;
import static org.assertj.core.api.Assertions.*;

/**
 * Created by raymo on 13/03/2017.
 */
public class TorrentFileProviderTest {

    private static final SeedManager.JoalFoldersPath joalFoldersPath = new SeedManager.JoalFoldersPath(Paths.get("src/test/resources/configtest"));

    private void resetDirectories() throws IOException {
        if (Files.exists(joalFoldersPath.getTorrentsDirPath())) {
            Files.walk(joalFoldersPath.getTorrentsDirPath(), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectory(joalFoldersPath.getTorrentsDirPath());
    }

    @AfterEach
    @BeforeEach
    public void setUpAndTearDown() throws IOException {
        resetDirectories();
    }

    @Test
    public void shouldNotBuildIfFolderDoesNotExists() {
        assertThatThrownBy(() -> new TorrentFileProvider(new SeedManager.JoalFoldersPath(Paths.get("nop"))))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageMatching("^Torrent folder \\[.*] not found$");
    }

    @Test
    public void shouldCreateArchiveFolderIfNotCreatedAlready() throws IOException {
        Files.deleteIfExists(joalFoldersPath.getTorrentArchiveDirPath());
        assertThat(exists(joalFoldersPath.getTorrentArchiveDirPath())).isFalse();
        new TorrentFileProvider(joalFoldersPath).init();
        assertThat(exists(joalFoldersPath.getTorrentArchiveDirPath())).isTrue();
    }

    @Test
    public void shouldFailIfFolderDoesNotContainsTorrentFiles() throws IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);

        assertThatThrownBy(() -> provider.getTorrentNotIn(new ArrayList<>()))
                .isInstanceOf(NoMoreTorrentsFileAvailableException.class)
                .hasMessageContaining("No more torrent file available");
    }

    @Test
    public void shouldAddFileToListOnCreation() throws IOException {
        TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        assertThat(provider.getTorrentCount()).isEqualTo(0);

        provider.onFileCreate(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldNotAddDuplicatedFiles() throws IOException {
        TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        assertThat(provider.getTorrentCount()).isEqualTo(0);

        provider.onFileCreate(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent").toFile());
        provider.onFileCreate(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldRemoveFileFromListOnDeletion() throws IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ninja.torrent"), TorrentFileCreator.TorrentType.NINJA_HEAT);

        assertThat(provider.getTorrentCount()).isEqualTo(0);
        provider.onFileCreate(joalFoldersPath.getTorrentsDirPath().resolve("ninja.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        provider.onFileDelete(joalFoldersPath.getTorrentsDirPath().resolve("ninja.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(0);
    }

    @Test
    public void shouldRemoveThenAddFileToListOnUpdate() throws IOException {
        TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);

        provider.onFileCreate(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        provider.onFileChange(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldMoveTorrentFileToArchivedFolderFromInfoHash() throws IOException, NoMoreTorrentsFileAvailableException {
        TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        provider.init();
        provider.onFileCreate(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        assertThat(joalFoldersPath.getTorrentArchiveDirPath().resolve("ubuntu.torrent")).doesNotExist();
        provider.moveToArchiveFolder(provider.getTorrentFiles().get(0).getTorrentInfoHash());
        assertThat(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent")).doesNotExist();

        assertThat(joalFoldersPath.getTorrentArchiveDirPath().resolve("ubuntu.torrent")).exists();
    }

    @Test
    public void shouldMoveTorrentFileToArchivedFolder() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        provider.init();
        provider.onFileCreate(torrentFile.toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        assertThat(joalFoldersPath.getTorrentArchiveDirPath().resolve("ubuntu.torrent")).doesNotExist();
        provider.moveToArchiveFolder(torrentFile.toFile());
        assertThat(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent")).doesNotExist();

        assertThat(joalFoldersPath.getTorrentArchiveDirPath().resolve("ubuntu.torrent")).exists();
    }

    @Test
    public void shouldNotFailIfFileIsNotPresentWhenArchiving() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);

        try {
            provider.moveToArchiveFolder(torrentFile.resolve("dd.torrent").toFile());
        } catch (final Throwable throwable) {
            fail("should not fail if file were not present");
        }
    }

    @Test
    public void shouldCallOnFileDeleteBeforeDeletingFileWhenArchiving() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = Mockito.spy(new TorrentFileProvider(joalFoldersPath));
        provider.init();
        Mockito.doAnswer(invocation -> {
            assertThat(torrentFile.toFile()).exists();
            return null;
        }).when(provider).onFileDelete(torrentFile.toFile());

        provider.onFileCreate(torrentFile.toFile());
        provider.moveToArchiveFolder(torrentFile.toFile());
        Mockito.verify(provider, Mockito.times(1)).moveToArchiveFolder(torrentFile.toFile());
        assertThat(torrentFile.toFile()).doesNotExist();
    }

    @Test
    public void shouldNotifyListenerOnFileAdded() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        provider.start();


        final CountDownLatch createLock = new CountDownLatch(1);
        final CountDownLatch deleteLock = new CountDownLatch(1);
        final TorrentFileChangeAware listener = new CountDownLatchListener(createLock, deleteLock);
        provider.registerListener(listener);

        provider.onFileCreate(torrentFile.toFile());

        assertThat(createLock.getCount()).isEqualTo(0);
        assertThat(deleteLock.getCount()).isEqualTo(1);
        provider.stop();
        provider.unRegisterListener(listener);
    }

    @Test
    public void shouldNotifyListenerOnFileRemoved() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        provider.onFileCreate(torrentFile.toFile());

        final CountDownLatch createLock = new CountDownLatch(1);
        final CountDownLatch deleteLock = new CountDownLatch(1);
        final TorrentFileChangeAware listener = new CountDownLatchListener(createLock, deleteLock);
        provider.registerListener(listener);

        provider.onFileDelete(torrentFile.toFile());
        provider.unRegisterListener(listener);

        assertThat(createLock.getCount()).isEqualTo(1);
        assertThat(deleteLock.getCount()).isEqualTo(0);
    }

    @Test
    public void shouldNotifyListenerOnFileChanged() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        provider.start();

        final CountDownLatch createLock = new CountDownLatch(1);
        final CountDownLatch deleteLock = new CountDownLatch(1);
        final TorrentFileChangeAware listener = new CountDownLatchListener(createLock, deleteLock);
        provider.registerListener(listener);

        provider.onFileChange(torrentFile.toFile());

        assertThat(createLock.getCount()).isEqualTo(0);
        assertThat(deleteLock.getCount()).isEqualTo(0);
        provider.stop();
        provider.unRegisterListener(listener);
    }

    @Test
    public void shouldUnRegisterListener() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final Path torrentFile2 = TorrentFileCreator.create(joalFoldersPath.getTorrentsDirPath().resolve("audio.torrent"), TorrentFileCreator.TorrentType.AUDIO);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        provider.start();

        final CountDownLatch createLock = new CountDownLatch(2);
        final CountDownLatch deleteLock = new CountDownLatch(2);
        final TorrentFileChangeAware listener = new CountDownLatchListener(createLock, deleteLock);
        provider.registerListener(listener);

        provider.onFileCreate(torrentFile.toFile());
        provider.unRegisterListener(listener);
        assertThat(createLock.getCount()).isEqualTo(1);
        provider.onFileCreate(torrentFile2.toFile());

        assertThat(createLock.getCount()).isEqualTo(1);
        assertThat(deleteLock.getCount()).isEqualTo(2);
        provider.stop();
        provider.unRegisterListener(listener);
    }


    private static final class CountDownLatchListener implements TorrentFileChangeAware {

        private final CountDownLatch createLock;
        private final CountDownLatch deleteLock;

        private CountDownLatchListener(final CountDownLatch createLock, final CountDownLatch deleteLock) {
            this.createLock = createLock;
            this.deleteLock = deleteLock;
        }

        @Override
        public void onTorrentFileAdded(final MockedTorrent torrent) {
            createLock.countDown();
        }

        @Override
        public void onTorrentFileRemoved(final MockedTorrent torrent) {
            deleteLock.countDown();
        }

    }

}
