package org.araymond.joal.core.ttorrent.client;

import com.google.common.collect.Lists;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
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
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnouncerExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;
import org.springframework.context.ApplicationEventPublisher;

import java.time.temporal.TemporalUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ClientTest {

    private AppConfiguration createMockedConf() {
        // wrap into spy to allow mocking
        return spy(AppConfigurationTest.createOne());
    }

    private AnnouncerFactory createMockedAnnouncerFactory() {
        final AnnouncerFactory announcerFactory = mock(AnnouncerFactory.class);
        doAnswer(invocation -> {
            final Announcer announcer = mock(Announcer.class);
            final InfoHash infoHash = ((MockedTorrent) invocation.getArguments()[0]).getTorrentInfoHash();
            doReturn(infoHash).when(announcer).getTorrentInfoHash();
            return announcer;
        }).when(announcerFactory).create(any(MockedTorrent.class));

        return announcerFactory;
    }

    private TorrentFileProvider createMockedTorrentFileProviderWithTorrent(final Iterable<MockedTorrent> mockedTorrents) {
        final TorrentFileProvider torrentFileProvider = mock(TorrentFileProvider.class);
        Stubber stubber = null;
        for (final MockedTorrent torrent : mockedTorrents) {
            if (stubber == null) {
                stubber = doReturn(torrent);
            } else {
                stubber = stubber.doReturn(torrent);
            }
        }
        try {
            if (stubber == null) {
                Mockito.doThrow(new NoMoreTorrentsFileAvailableException("no more")).when(torrentFileProvider).getTorrentNotIn(anyList());
            } else {
                stubber
                        .doThrow(new NoMoreTorrentsFileAvailableException("no more"))
                        .when(torrentFileProvider).getTorrentNotIn(anyList());
            }
        } catch (final NoMoreTorrentsFileAvailableException ignore) {
        }

        return torrentFileProvider;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDeleteTorrentIfNoMorePeersOnlyIfConfigValueShouldKeepInactiveTorrentIsFalse() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(false).when(appConfiguration).isKeepTorrentWithZeroLeechers();

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

        verify(torrentFileProvider, times(1)).moveToArchiveFolder(eq(infoHash));

        final InfoHash infoHash2 = InfoHashTest.createOne("abcd");
        doReturn(true).when(appConfiguration).isKeepTorrentWithZeroLeechers();
        client.onNoMorePeers(infoHash);

        verify(torrentFileProvider, times(1)).moveToArchiveFolder(eq(infoHash2));
    }

    @SuppressWarnings({"unchecked", "TypeMayBeWeakened", "ResultOfMethodCallIgnored", "ConstantConditions"})
    @Test
    public void shouldStartAnnouncerWhenStart() {
        // given
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

        // when
        client.start();

        // then
        Mockito.verify(delayQueue, times(appConfiguration.getSimultaneousSeed()))
                .addOrReplace(any(AnnounceRequest.class), anyInt(), any(TemporalUnit.class));
        assertThat(client.getCurrentlySeedingAnnouncers().stream()
                .map(announcer -> announcer.getTorrentInfoHash().value())
                .reduce((o, t) -> o + t).get()
        ).isEqualTo("abcdefghijklmno");
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    @Test
    public void shouldNotFailToAddTorrentIfThereIsNoMoreTorrentFileAvailable() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(5).when(appConfiguration).getSimultaneousSeed();

        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Collections.emptyList());

        final AnnouncerFactory mockedAnnouncerFactory = createMockedAnnouncerFactory();

        final Client client = (Client) ClientBuilder.builder()
                .withAnnouncerFactory(mockedAnnouncerFactory)
                .withBandwidthDispatcher(mock(BandwidthDispatcher.class))
                .withAppConfiguration(appConfiguration)
                .withTorrentFileProvider(torrentFileProvider)
                .withEventPublisher(mock(ApplicationEventPublisher.class))
                .withDelayQueue(mock(DelayQueue.class))
                .build();

        try {
            client.start();
        } catch (final Throwable t) {
            fail("should not fail if no torrents are available");
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    @Test
    public void shouldClearDelayQueueOnStopAndSendStopAnnounceToExecutor() throws AnnounceException, TooManyAnnouncesFailedInARowException {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(1).when(appConfiguration).getSimultaneousSeed();

        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                MockedTorrentTest.createOneMock("abc")
        ));

        final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        doReturn(Lists.newArrayList(AnnounceRequest.createRegular(null))).when(delayQueue).drainAll();
        final AnnouncerFactory mockedAnnouncerFactory = mock(AnnouncerFactory.class);

        final Client client = (Client) ClientBuilder.builder()
                .withAnnouncerFactory(mockedAnnouncerFactory)
                .withBandwidthDispatcher(mock(BandwidthDispatcher.class))
                .withAppConfiguration(appConfiguration)
                .withTorrentFileProvider(torrentFileProvider)
                .withEventPublisher(mock(ApplicationEventPublisher.class))
                .withDelayQueue(delayQueue)
                .build();

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);

        client.start();
        verify(delayQueue, times(1)).addOrReplace(any(AnnounceRequest.class), anyInt(), any(TemporalUnit.class));
        Thread.yield();

        client.stop();
        verify(delayQueue, times(1)).drainAll();
        verify(announcerExecutor, times(1)).execute(argThat(new ArgumentMatcher<AnnounceRequest>() {
            @Override
            public boolean matches(final AnnounceRequest argument) {
                return argument.getEvent() == RequestEvent.STOPPED;
            }
        }));
    }

    @Test
    public void shouldNotExecuteAnnounceStopForAnnouncerInDelayQueueThatAreAwaitingStartedWhenClientStop() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(1).when(appConfiguration).getSimultaneousSeed();

        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                MockedTorrentTest.createOneMock("abc")
        ));

        final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final AnnouncerFactory mockedAnnouncerFactory = mock(AnnouncerFactory.class);

        final Client client = (Client) ClientBuilder.builder()
                .withAnnouncerFactory(mockedAnnouncerFactory)
                .withBandwidthDispatcher(mock(BandwidthDispatcher.class))
                .withAppConfiguration(appConfiguration)
                .withTorrentFileProvider(torrentFileProvider)
                .withEventPublisher(mock(ApplicationEventPublisher.class))
                .withDelayQueue(delayQueue)
                .build();

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);

        client.start();
        verify(delayQueue, times(1)).addOrReplace(any(AnnounceRequest.class), anyInt(), any(TemporalUnit.class));
        Thread.yield();

        client.stop();
        verify(delayQueue, times(1)).drainAll();
        verify(announcerExecutor, times(0)).execute(argThat(new ArgumentMatcher<AnnounceRequest>() {
            @Override
            public boolean matches(final AnnounceRequest argument) {
                return argument.getEvent() == RequestEvent.STOPPED;
            }
        }));
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    @Test
    public void shouldAwaitAnnounceExecutorOnStop() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(0).when(appConfiguration).getSimultaneousSeed();

        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                MockedTorrentTest.createOneMock("abc")
        ));

        final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final AnnouncerFactory mockedAnnouncerFactory = mock(AnnouncerFactory.class);

        final Client client = (Client) ClientBuilder.builder()
                .withAnnouncerFactory(mockedAnnouncerFactory)
                .withBandwidthDispatcher(mock(BandwidthDispatcher.class))
                .withAppConfiguration(appConfiguration)
                .withTorrentFileProvider(torrentFileProvider)
                .withEventPublisher(mock(ApplicationEventPublisher.class))
                .withDelayQueue(delayQueue)
                .build();

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);

        client.start();

        client.stop();
        verify(announcerExecutor, times(1)).awaitForRunningTasks();
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    @Test
    public void shouldRemoveAnnouncerFromRunningListOnTooManyFailsAndEnqueueAnother() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(1).when(appConfiguration).getSimultaneousSeed();

        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                MockedTorrentTest.createOneMock("abc"),
                MockedTorrentTest.createOneMock("def")
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

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);
        final ArgumentCaptor<AnnounceRequest> argumentCaptor = ArgumentCaptor.forClass(AnnounceRequest.class);

        client.start();
        verify(delayQueue, times(1)).addOrReplace(argumentCaptor.capture(), anyInt(), any(TemporalUnit.class));
        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);

        final Announcer firstAnnouncer = argumentCaptor.getValue().getAnnouncer();

        client.onTooManyFailedInARaw(firstAnnouncer);
        verify(torrentFileProvider, times(1)).moveToArchiveFolder(eq(firstAnnouncer.getTorrentInfoHash()));
        verify(delayQueue, times(2)).addOrReplace(argumentCaptor.capture(), anyInt(), any(TemporalUnit.class));
        assertThat(argumentCaptor.getValue().getInfoHash()).isNotEqualTo(firstAnnouncer.getTorrentInfoHash());
        assertThat(argumentCaptor.getValue().getInfoHash().value()).isEqualTo("def");
        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    @Test
    public void shouldTryToAddAnotherTorrentAfterAnAnnouncerHasStopped() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(1).when(appConfiguration).getSimultaneousSeed();

        final MockedTorrent torrent = MockedTorrentTest.createOneMock("abc");
        final MockedTorrent torrent2 = MockedTorrentTest.createOneMock("abc");
        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                torrent,
                torrent2
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

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);

        client.start();
        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);
        final ArgumentCaptor<AnnounceRequest> argumentCaptor = ArgumentCaptor.forClass(AnnounceRequest.class);
        verify(delayQueue, times(1)).addOrReplace(argumentCaptor.capture(), anyInt(), any(TemporalUnit.class));

        Mockito.reset(delayQueue);
        client.onTorrentHasStopped(argumentCaptor.getValue().getAnnouncer());
        verify(delayQueue, times(1)).addOrReplace(argumentCaptor.capture(), anyInt(), any(TemporalUnit.class));

        final AnnounceRequest announceRequest = argumentCaptor.getValue();
        assertThat(announceRequest.getInfoHash()).isEqualTo(torrent2.getTorrentInfoHash());
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.STARTED);
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    @Test
    public void shouldAddTorrentWhenATorrentFileIsCreatedAnClientNotAlreadySeedingOnMaxSimultaneousSeed() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(2).when(appConfiguration).getSimultaneousSeed();

        final MockedTorrent torrent2 = MockedTorrentTest.createOneMock("def");
        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                MockedTorrentTest.createOneMock("abc")
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

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);

        client.start();
        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);


        final ArgumentCaptor<AnnounceRequest> argumentCaptor = ArgumentCaptor.forClass(AnnounceRequest.class);

        Mockito.reset(delayQueue);
        client.onTorrentFileAdded(torrent2);
        verify(delayQueue, times(1)).addOrReplace(argumentCaptor.capture(), anyInt(), any(TemporalUnit.class));

        final AnnounceRequest announceRequest = argumentCaptor.getValue();
        assertThat(announceRequest.getInfoHash()).isEqualTo(torrent2.getTorrentInfoHash());
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.STARTED);

        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(2);
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    @Test
    public void shouldNotAddTorrentWhenATorrentFileIsCreatedAnClientAlreadySeedingOnMaxSimultaneousSeed() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(1).when(appConfiguration).getSimultaneousSeed();

        final MockedTorrent torrent3 = MockedTorrentTest.createOneMock("ghi");
        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                MockedTorrentTest.createOneMock("abc"),
                MockedTorrentTest.createOneMock("def")
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

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);

        client.start();
        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);

        Mockito.reset(delayQueue);
        client.onTorrentFileAdded(torrent3);
        verify(delayQueue, times(0)).addOrReplace(any(AnnounceRequest.class), anyInt(), any(TemporalUnit.class));

        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);
    }


    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    @Test
    public void shouldTryToStopAnnouncerWhenTorrentFileIsDeleted() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(1).when(appConfiguration).getSimultaneousSeed();

        final MockedTorrent torrent = MockedTorrentTest.createOneMock("abc");
        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                torrent
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

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);

        client.start();
        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);


        final ArgumentCaptor<AnnounceRequest> argumentCaptor = ArgumentCaptor.forClass(AnnounceRequest.class);

        Mockito.reset(delayQueue);
        client.onTorrentFileRemoved(torrent);
        verify(delayQueue, times(1)).addOrReplace(argumentCaptor.capture(), anyInt(), any(TemporalUnit.class));

        final AnnounceRequest announceRequest = argumentCaptor.getValue();
        assertThat(announceRequest.getInfoHash()).isEqualTo(torrent.getTorrentInfoHash());
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.STOPPED);
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    @Test
    public void shouldNotFailWhenATorrentFileIsRemovedButNoAnnouncerIsStartedForThisFile() {
        final AppConfiguration appConfiguration = this.createMockedConf();
        doReturn(1).when(appConfiguration).getSimultaneousSeed();

        final TorrentFileProvider torrentFileProvider = createMockedTorrentFileProviderWithTorrent(Lists.newArrayList(
                MockedTorrentTest.createOneMock("abc")
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

        final AnnouncerExecutor announcerExecutor = mock(AnnouncerExecutor.class);
        client.setAnnouncerExecutor(announcerExecutor);

        client.start();
        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);

        try {
            client.onTorrentFileRemoved(MockedTorrentTest.createOneMock("def"));
        } catch (final Throwable t) {
            fail("Should not have thrown");
        }
        assertThat(client.getCurrentlySeedingAnnouncers()).hasSize(1);
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
                .registerListener(eq(client));

        client.stop();
        Mockito.verify(torrentFileProvider, times(1))
                .unRegisterListener(eq(client));
    }

}
