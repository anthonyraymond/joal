package org.araymond.joal.core.torrent.watcher;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.araymond.joal.core.utils.TorrentFileCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by raymo on 01/05/2017.
 */
public class TorrentFileWatcherTest {

    private static final Path torrentsPath = Paths.get("src/test/resources/configtest").resolve("torrents");

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
    public void shouldNotBuildWithNullListener() {
        assertThatThrownBy(() -> new TorrentFileWatcher(null, torrentsPath))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("listener cannot be null");
    }

    @Test
    public void shouldNotBuildWithNullMonitoredFolder() {
        assertThatThrownBy(() -> new TorrentFileWatcher(new FileAlterationListenerAdaptor(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("monitoredFolder cannot be null");
    }

    @Test
    public void shouldNotBuildWithNonExistingMonitoredFolder() {
        assertThatThrownBy(() -> new TorrentFileWatcher(new FileAlterationListenerAdaptor(), torrentsPath.resolve("nop")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Folder '" + torrentsPath.resolve("nop").toAbsolutePath() + "' does not exists.");
    }

    @Test
    public void shouldNotBuildWithNullInterval() {
        assertThatThrownBy(() -> new TorrentFileWatcher(new FileAlterationListenerAdaptor(), torrentsPath, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("interval cannot be null");
    }

    @Test
    public void shouldNotBuildWithIntervalLessThan1() {
        assertThatThrownBy(() -> new TorrentFileWatcher(new FileAlterationListenerAdaptor(), torrentsPath, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("interval cannot be less than 1");
    }

    @Test
    public void shouldBuild() {
        try {
            final TorrentFileWatcher watcher = new TorrentFileWatcher(new FileAlterationListenerAdaptor(), torrentsPath);
            watcher.start();
            watcher.stop();
        } catch (final Throwable e) {
            fail("Should Not fail");
        }
    }

    @Test
    public void shouldOnlyDetectTorrentFiles() throws IOException, InterruptedException {
        Files.createFile(torrentsPath.resolve("loop.txt"));
        Files.createFile(torrentsPath.resolve("loop.torrent.txt"));

        final TorrentFileWatcher watcher = new TorrentFileWatcher(
                new FailOnTriggerListener(),
                torrentsPath,
                5
        );
        watcher.start();

        Files.createFile(torrentsPath.resolve("loop2.txt"));
        Files.createFile(torrentsPath.resolve("loop2.torrent.txt"));
        Thread.sleep(15);

        watcher.stop();
    }

    @Test
    public void shouldNotifyOnCreate() throws InterruptedException, IOException {
        final CountDownLatch createLock = new CountDownLatch(1);
        final CountDownLatch changeLock = new CountDownLatch(1);
        final CountDownLatch deleteLock = new CountDownLatch(1);
        final TorrentFileWatcher watcher = new TorrentFileWatcher(
                new CountDownLatchListener(createLock, changeLock, deleteLock),
                torrentsPath,
                5
        );
        watcher.start();

        TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);

        assertThat(createLock.await(10, TimeUnit.MILLISECONDS)).isTrue();
        watcher.stop();

        assertThat(createLock.getCount()).isEqualTo(0);
        assertThat(changeLock.getCount()).isEqualTo(1);
        assertThat(deleteLock.getCount()).isEqualTo(1);
    }

    @Test
    public void shouldNotifyOnChange() throws IOException, InterruptedException {
        final CountDownLatch createLock = new CountDownLatch(1);
        final CountDownLatch changeLock = new CountDownLatch(1);
        final CountDownLatch deleteLock = new CountDownLatch(1);
        final TorrentFileWatcher watcher = new TorrentFileWatcher(
                new CountDownLatchListener(createLock, changeLock, deleteLock),
                torrentsPath,
                5
        );
        watcher.start();

        final Path torrent = TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        assertThat(createLock.await(10, TimeUnit.MILLISECONDS)).isTrue();

        if (!torrent.toFile().setLastModified(100)) {
            fail("failed to modify file date");
        }
        assertThat(changeLock.await(10, TimeUnit.MILLISECONDS)).isTrue();
        watcher.stop();

        assertThat(createLock.getCount()).isEqualTo(0);
        assertThat(changeLock.getCount()).isEqualTo(0);
        assertThat(deleteLock.getCount()).isEqualTo(1);
    }

    @Test
    public void shouldNotifyOnDelete() throws IOException, InterruptedException {
        final CountDownLatch createLock = new CountDownLatch(1);
        final CountDownLatch changeLock = new CountDownLatch(1);
        final CountDownLatch deleteLock = new CountDownLatch(1);
        final TorrentFileWatcher watcher = new TorrentFileWatcher(
                new CountDownLatchListener(createLock, changeLock, deleteLock),
                torrentsPath,
                5
        );
        watcher.start();

        final Path torrent = TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        assertThat(createLock.await(10, TimeUnit.MILLISECONDS)).isTrue();

        Files.delete(torrent);
        assertThat(deleteLock.await(10, TimeUnit.MILLISECONDS)).isTrue();
        watcher.stop();

        assertThat(createLock.getCount()).isEqualTo(0);
        assertThat(changeLock.getCount()).isEqualTo(1);
        assertThat(deleteLock.getCount()).isEqualTo(0);
    }

    @Test
    public void shouldDetectFileAlreadyPresentOnStart() throws IOException, InterruptedException {
        TorrentFileCreator.create(torrentsPath.resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        TorrentFileCreator.create(torrentsPath.resolve("ninja.torrent"), TorrentFileCreator.TorrentType.NINJA_HEAT);

        final CountDownLatch createLock = new CountDownLatch(2);
        final CountDownLatch changeLock = new CountDownLatch(1);
        final CountDownLatch deleteLock = new CountDownLatch(1);
        final TorrentFileWatcher watcher = new TorrentFileWatcher(
                new CountDownLatchListener(createLock, changeLock, deleteLock),
                torrentsPath,
                5
        );
        watcher.start();
        assertThat(createLock.await(10, TimeUnit.MILLISECONDS)).isTrue();
        watcher.stop();

        assertThat(createLock.getCount()).isEqualTo(0);
        assertThat(changeLock.getCount()).isEqualTo(1);
        assertThat(deleteLock.getCount()).isEqualTo(1);
    }

    @Test
    public void shouldNotWatchSubFolders() throws IOException, InterruptedException {
        Files.createDirectory(torrentsPath.resolve("sub-folder"));
        final TorrentFileWatcher watcher = new TorrentFileWatcher(
                new FailOnTriggerListener(),
                torrentsPath,
                5
        );
        watcher.start();

        TorrentFileCreator.create(torrentsPath.resolve("sub-folder").resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        Thread.sleep(15);

        watcher.stop();
    }

    @Test
    public void shouldNotDetectFilesInSubFolderOnStart() throws IOException {
        Files.createDirectory(torrentsPath.resolve("sub-folder"));
        TorrentFileCreator.create(torrentsPath.resolve("sub-folder").resolve("ubuntu.torrent"), TorrentFileCreator.TorrentType.UBUNTU);
        final TorrentFileWatcher watcher = new TorrentFileWatcher(
                new FailOnTriggerListener(),
                torrentsPath,
                5
        );
        watcher.start();
        watcher.stop();
    }

    @Test
    public void shouldFaiToStartWithNonExistingPath() throws IOException {
        final Path directory = Files.createDirectory(torrentsPath.resolve("sub-folder"));
        final TorrentFileWatcher torrentFileWatcher = new TorrentFileWatcher(
                new FileAlterationListenerAdaptor(),
                directory,
                10
        );

        Files.delete(directory);

        assertThatThrownBy(torrentFileWatcher::start)
                .isInstanceOf(IllegalStateException.class);
    }

    private static final class FailOnTriggerListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileCreate(final File file) {
            fail("Should not have had detected");
        }

        @Override
        public void onFileChange(final File file) {
            fail("Should not have had detected");
        }

        @Override
        public void onFileDelete(final File file) {
            fail("Should not have had detected");
        }
    }

    private static final class CountDownLatchListener extends FileAlterationListenerAdaptor {

        private final CountDownLatch createLock;
        private final CountDownLatch changeLock;
        private final CountDownLatch deleteLock;

        private CountDownLatchListener(final CountDownLatch createLock, final CountDownLatch changeLock, final CountDownLatch deleteLock) {
            this.createLock = createLock;
            this.changeLock = changeLock;
            this.deleteLock = deleteLock;
        }

        @Override
        public void onFileCreate(final File file) {
            createLock.countDown();
        }

        @Override
        public void onFileChange(final File file) {
            changeLock.countDown();
        }

        @Override
        public void onFileDelete(final File file) {
            deleteLock.countDown();
        }
    }

}
