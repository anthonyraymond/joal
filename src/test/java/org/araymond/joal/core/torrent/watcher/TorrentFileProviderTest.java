package org.araymond.joal.core.torrent.watcher;

import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
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
        if (Files.exists(joalFoldersPath.getTorrentFilesPath())) {
            Files.walk(joalFoldersPath.getTorrentFilesPath(), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectory(joalFoldersPath.getTorrentFilesPath());
    }

    @After
    @Before
    public void setUpAndTearDown() throws IOException {
        resetDirectories();
    }

    @Test
    public void shouldNotBuildIfFolderDoesNotExists() throws FileNotFoundException {
        assertThatThrownBy(() -> new TorrentFileProvider(new SeedManager.JoalFoldersPath(Paths.get("nop"))))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageStartingWith("Torrent folder '")
                .hasMessageEndingWith("' not found.");
    }

    @Test
    public void shouldCreateArchiveFolderIfNotCreatedAlready() throws IOException {
        Files.deleteIfExists(joalFoldersPath.getTorrentArchivedPath());
        assertThat(exists(joalFoldersPath.getTorrentArchivedPath())).isFalse();
        new TorrentFileProvider(joalFoldersPath).init();
        assertThat(exists(joalFoldersPath.getTorrentArchivedPath())).isTrue();
    }

    @Test
    public void shouldFailIfFolderDoesNotContainsTorrentFiles() throws IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);

        assertThatThrownBy(() -> provider.getTorrentNotIn(new ArrayList<>()))
                .isInstanceOf(NoMoreTorrentsFileAvailableException.class)
                .hasMessageContaining("No more torrent file available.");
    }

    @Test
    public void shouldAddFileToListOnCreation() throws IOException {
        TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        assertThat(provider.getTorrentCount()).isEqualTo(0);

        provider.onFileCreate(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldNotAddDuplicatedFiles() throws IOException {
        TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        assertThat(provider.getTorrentCount()).isEqualTo(0);

        provider.onFileCreate(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent").toFile());
        provider.onFileCreate(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldRemoveFileFromListOnDeletion() throws IOException {
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ninja.torrent"), TorrentFileCreator.TorrentType.NINJA_HEAT);

        assertThat(provider.getTorrentCount()).isEqualTo(0);
        provider.onFileCreate(joalFoldersPath.getTorrentFilesPath().resolve("ninja.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        provider.onFileDelete(joalFoldersPath.getTorrentFilesPath().resolve("ninja.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(0);
    }

    @Test
    public void shouldRemoveThenAddFileToListOnUpdate() throws IOException {
        TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);

        provider.onFileCreate(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        provider.onFileChange(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldMoveTorrentFileToArchivedFolderFromMockedTorrent() throws IOException, NoMoreTorrentsFileAvailableException {
        TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        provider.init();
        provider.onFileCreate(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent").toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        assertThat(joalFoldersPath.getTorrentArchivedPath().resolve("ubuntu.torrent")).doesNotExist();
        provider.moveToArchiveFolder(provider.getTorrentNotIn(new ArrayList<>()));
        assertThat(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent")).doesNotExist();

        assertThat(joalFoldersPath.getTorrentArchivedPath().resolve("ubuntu.torrent")).exists();
    }

    @Test
    public void shouldMoveTorrentFileToArchivedFolder() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);
        provider.init();
        provider.onFileCreate(torrentFile.toFile());
        assertThat(provider.getTorrentCount()).isEqualTo(1);

        assertThat(joalFoldersPath.getTorrentArchivedPath().resolve("ubuntu.torrent")).doesNotExist();
        provider.moveToArchiveFolder(torrentFile.toFile());
        assertThat(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent")).doesNotExist();

        assertThat(joalFoldersPath.getTorrentArchivedPath().resolve("ubuntu.torrent")).exists();
    }

    @Test
    public void shouldNotFailIfFileIsNotPresentWhenArchiving() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        final TorrentFileProvider provider = new TorrentFileProvider(joalFoldersPath);

        try {
            provider.moveToArchiveFolder(torrentFile.resolve("dd.torrent").toFile());
        } catch (final Throwable throwable) {
            fail("should not fail if file were not present.");
        }
    }

    @Test
    public void shouldCallOnFileDeleteBeforeDeletingFileWhenArchiving() throws IOException {
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

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
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

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
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

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
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

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
        final Path torrentFile = TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final Path torrentFile2 = TorrentFileCreator.create(joalFoldersPath.getTorrentFilesPath().resolve("audio.torrent"), TorrentFileCreator.TorrentType.AUDIO);

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

        @Override
        public void onInvalidTorrentFile(final String fileName, final String errMessage) {

        }
    }

}
