package org.araymond.joal.core.ttorrent.client;

import com.google.common.collect.Lists;
import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.AppConfigurationTest;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.InfoHashTest;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.torrent.torrent.MockedTorrentTest;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFactory;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;
import org.springframework.context.ApplicationEventPublisher;

import java.time.temporal.TemporalUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ClientTest {

    private AppConfiguration createMockedConf() {
        // wrap into spy to allow mocking
        return Mockito.spy(AppConfigurationTest.createOne());
    }

    private AnnouncerFactory createMockedAnnouncerFactory() {
        final AnnouncerFactory announcerFactory = mock(AnnouncerFactory.class);
        doAnswer(invocation -> {
            final Announcer announcer = mock(Announcer.class);
            final InfoHash infoHash = ((MockedTorrent) invocation.getArguments()[0]).getTorrentInfoHash();
            doReturn(infoHash).when(announcer).getTorrentInfoHash();
            return announcer;
        }).when(announcerFactory).create(Matchers.any(MockedTorrent.class));

        return announcerFactory;
    }

    private TorrentFileProvider createMockedTorrentFileProviderWithTorrent(final Iterable<MockedTorrent> mockedTorrents) {
        final TorrentFileProvider torrentFileProvider = mock(TorrentFileProvider.class);
        Stubber stubber = null;
        for (final MockedTorrent torrent : mockedTorrents) {
            if (stubber == null) {
                stubber = Mockito.doReturn(torrent);
            } else {
                stubber = stubber.doReturn(torrent);
            }
        }
        try {
            if (stubber == null) {
                Mockito.doThrow(new NoMoreTorrentsFileAvailableException("no more")).when(torrentFileProvider).getTorrentNotIn(Matchers.anyListOf(InfoHash.class));
            } else {
                stubber
                        .doThrow(new NoMoreTorrentsFileAvailableException("no more"))
                        .when(torrentFileProvider).getTorrentNotIn(Matchers.anyListOf(InfoHash.class));
            }
        } catch (final NoMoreTorrentsFileAvailableException ignore) {
        }

        return torrentFileProvider;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDeleteTorrentIfNoMorePeersOnlyIfConfigValueShouldKeepInactiveTorrentIsFalse() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(false).when(appConfiguration).shouldKeepTorrentWithZeroLeechers();

        final TorrentFileProvider torrentFileProvider = mock(TorrentFileProvider.class);

        final Client client = (Client) ClientBuilder.builder()
                .withAnnouncerFactory(mock(AnnouncerFactory.class))
                .withBandwidthDispatcher(mock(BandwidthDispatcher.class))
                .withAppConfiguration(appConfiguration)
                .withTorrentFileProvider(torrentFileProvider)
                .withEventPublisher(mock(ApplicationEventPublisher.class))
                .withDelayQueue(mock(DelayQueue.class))
                .build();

        final InfoHash infoHash = InfoHashTest.createOne("abcd");
        client.onNoMorePeers(infoHash);

        verify(torrentFileProvider, times(1)).moveToArchiveFolder(Matchers.eq(infoHash));

        final InfoHash infoHash2 = InfoHashTest.createOne("abcd");
        doReturn(true).when(appConfiguration).shouldKeepTorrentWithZeroLeechers();
        client.onNoMorePeers(infoHash);

        verify(torrentFileProvider, times(1)).moveToArchiveFolder(Matchers.eq(infoHash2));
    }

    @SuppressWarnings({"unchecked", "TypeMayBeWeakened", "ResultOfMethodCallIgnored", "ConstantConditions"})
    @Test
    public void shouldStartAnnouncerWhenStart() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(5).when(appConfiguration).getSimultaneousSeed();

        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                MockedTorrentTest.createOneMock("abc"),
                MockedTorrentTest.createOneMock("def"),
                MockedTorrentTest.createOneMock("ghi"),
                MockedTorrentTest.createOneMock("jkl"),
                MockedTorrentTest.createOneMock("mno")
        ));

        final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final AnnouncerFactory mockedAnnouncerFactory = createMockedAnnouncerFactory();

        final Client client = (Client) ClientBuilder.builder()
                .withAnnouncerFactory(mockedAnnouncerFactory)
                .withBandwidthDispatcher(mock(BandwidthDispatcher.class))
                .withAppConfiguration(appConfiguration)
                .withTorrentFileProvider(torrentFileProvider)
                .withEventPublisher(mock(ApplicationEventPublisher.class))
                .withDelayQueue(delayQueue)
                .build();

        client.start();

        Mockito.verify(delayQueue, times(appConfiguration.getSimultaneousSeed()))
                .addOrReplace(Mockito.any(AnnounceRequest.class), Mockito.anyInt(), Mockito.any(TemporalUnit.class));
        assertThat(client.getCurrentlySeedingAnnouncer().stream()
                .map(announcer -> announcer.getTorrentInfoHash().value())
                .reduce((o, t) -> o + t).get()
        ).isEqualTo("abcdefghijklmno");
    }

    @Test
    public void shouldNotFailToAddTorrentIfThereIsNoMoreTorrentFileAvailable() {
        // TODO : implement
    }

    @Test
    public void shouldClearDelayQueueOnStopAndSendStopAnnounceToExecutor() {

    }

    @Test
    public void shouldAwaitAnnounceExecutorOnStop() {

    }

    @Test
    public void shouldRemoveAnnouncerFromRunningListOnTooManyFails() {

    }

    @Test
    public void shouldMoveToArchiveFolderOnTooManyFails() {

    }

    @Test
    public void shouldTryToAddAnotherTorrentAfterTooManyFails() {

    }

    @Test
    public void shouldTryToAddAnotherTorrentAfterAnAnnouncerHasStopped() {

    }

    @Test
    public void shouldAddTorrentWhenATorrentFileIsCreatedAnClientNotAlreadySeedingOnMaxSimultaneousSeed() {

    }

    @Test
    public void shouldNotAddTorrentWhenATorrentFileIsCreatedAnClientAlreadySeedingOnMaxSimultaneousSeed() {

    }

    @Test
    public void shouldTryToStopAnnouncerWhenTorrentFileIsDeleted() {

    }

    @Test
    public void shouldNotFailWhenATorrentFileIsRemovedButNoAnnouncerIsStartedForThisFile() {

    }


    @SuppressWarnings({"unchecked", "TypeMayBeWeakened", "ResultOfMethodCallIgnored", "ConstantConditions"})
    @Test
    public void shouldRegisterToTorrentFileProviderOnStartAndUnregisterOnStop() {
        final AppConfiguration appConfiguration = this.createMockedConf();

        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList());

        final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final AnnouncerFactory mockedAnnouncerFactory = createMockedAnnouncerFactory();
        final BandwidthDispatcher bandwidthDispatcher = mock(BandwidthDispatcher.class);

        final Client client = (Client) ClientBuilder.builder()
                .withAnnouncerFactory(mockedAnnouncerFactory)
                .withBandwidthDispatcher(bandwidthDispatcher)
                .withAppConfiguration(appConfiguration)
                .withTorrentFileProvider(torrentFileProvider)
                .withEventPublisher(mock(ApplicationEventPublisher.class))
                .withDelayQueue(delayQueue)
                .build();

        client.start();
        Mockito.verify(torrentFileProvider, times(1))
                .registerListener(Matchers.eq(client));

        client.stop();
        Mockito.verify(torrentFileProvider, times(1))
                .unRegisterListener(Matchers.eq(client));
    }

}
