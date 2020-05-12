package org.araymond.joal.core.ttorrent.client.announcer;

import com.google.common.collect.Lists;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.http.client.HttpClient;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.torrent.torrent.MockedTorrentTest;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponseTest;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.TrackerClient;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AnnouncerTest {

    @Test
    public void shouldProvideRequiredInfoForAnnouncerFacade() {
        final MockedTorrent torrent = MockedTorrentTest.createOneMock();
        final AnnouncerFacade facade = new Announcer(torrent, null, Mockito.mock(HttpClient.class));

        assertThat(facade.getConsecutiveFails()).isEqualTo(0);
        assertThat(facade.getLastKnownInterval()).isEqualTo(5);
        assertThat(facade.getTorrentName()).isEqualTo(torrent.getName());
        assertThat(facade.getTorrentSize()).isEqualTo(torrent.getSize());
        assertThat(facade.getTorrentInfoHash()).isEqualTo(torrent.getTorrentInfoHash());
    }

    @Test
    public void shouldThrowTooManyFailsExceptionIfFailsFiveTimesInARaw() throws AnnounceException {
        final MockedTorrent torrent = MockedTorrentTest.createOneMock("abcd");

        final TrackerClient trackerClient = mock(TrackerClient.class);
        doThrow(new AnnounceException("yeah ! :)")).when(trackerClient).announce(anyString(), any());
        final AnnounceDataAccessor dataAccessor = mock(AnnounceDataAccessor.class);
        doReturn("dd=ff&qq=d").when(dataAccessor).getHttpRequestQueryForTorrent(any(InfoHash.class), eq(RequestEvent.STARTED));
        doReturn(Lists.newArrayList()).when(dataAccessor).getHttpHeadersForTorrent();

        final Announcer announcer = new Announcer(torrent, dataAccessor, Mockito.mock(HttpClient.class));
        announcer.setTrackerClient(trackerClient);

        //noinspection Duplicates
        for (int i = 0; i < 4; ++i) {
            try {
                announcer.announce(RequestEvent.STARTED);
                fail("Should have thrown AnnounceException");
            } catch (final AnnounceException ignore) {
            } catch (final TooMuchAnnouncesFailedInARawException e) {
                fail("should not have thrown TooMuchAnnouncesFailedInARawException already");
            }
        }

        assertThatThrownBy(() -> announcer.announce(RequestEvent.STARTED))
                .isInstanceOf(TooMuchAnnouncesFailedInARawException.class);
    }

    @Test
    public void shouldResetConsecutiveFailsOnAnnounceSuccess() throws AnnounceException {
        final MockedTorrent torrent = MockedTorrentTest.createOneMock("abcd");

        final TrackerClient trackerClient = mock(TrackerClient.class);
        doThrow(new AnnounceException("yeah ! :)"))
                .doThrow(new AnnounceException("yeah ! :)"))
                .doThrow(new AnnounceException("yeah ! :)"))
                .doThrow(new AnnounceException("yeah ! :)"))
                .doReturn(SuccessAnnounceResponseTest.createOne())
                .when(trackerClient).announce(anyString(), any());
        final AnnounceDataAccessor dataAccessor = mock(AnnounceDataAccessor.class);
        doReturn("dd=ff&qq=d").when(dataAccessor).getHttpRequestQueryForTorrent(any(InfoHash.class), eq(RequestEvent.STARTED));
        doReturn(Lists.newArrayList()).when(dataAccessor).getHttpHeadersForTorrent();

        final Announcer announcer = new Announcer(torrent, dataAccessor, Mockito.mock(HttpClient.class));
        announcer.setTrackerClient(trackerClient);

        //noinspection Duplicates
        for (int i = 0; i < 4; ++i) {
            try {
                announcer.announce(RequestEvent.STARTED);
                fail("Should have thrown AnnounceException");
            } catch (final AnnounceException ignore) {
            } catch (final TooMuchAnnouncesFailedInARawException e) {
                fail("should not have thrown TooMuchAnnouncesFailedInARawException already");
            }
        }
        assertThat(announcer.getConsecutiveFails()).isEqualTo(4);

        try {
            announcer.announce(RequestEvent.STARTED);
        } catch (final TooMuchAnnouncesFailedInARawException | AnnounceException e) {
            fail("should not have failed");
        }

        assertThat(announcer.getConsecutiveFails()).isEqualTo(0);
    }

    @Test
    public void shouldUpdateAnnounceDateOnEachAnnounce() throws AnnounceException, InterruptedException {
        final MockedTorrent torrent = MockedTorrentTest.createOneMock("abcd");

        final TrackerClient trackerClient = mock(TrackerClient.class);
        doThrow(new AnnounceException("yeah ! :)"))
                .doReturn(SuccessAnnounceResponseTest.createOne())
                .when(trackerClient).announce(anyString(), any());
        final AnnounceDataAccessor dataAccessor = mock(AnnounceDataAccessor.class);
        doReturn("dd=ff&qq=d").when(dataAccessor).getHttpRequestQueryForTorrent(any(InfoHash.class), eq(RequestEvent.STARTED));
        doReturn(Lists.newArrayList()).when(dataAccessor).getHttpHeadersForTorrent();

        final Announcer announcer = new Announcer(torrent, dataAccessor, Mockito.mock(HttpClient.class));
        announcer.setTrackerClient(trackerClient);

        assertThat(announcer.getLastAnnouncedAt()).isEmpty();
        try {
            announcer.announce(RequestEvent.STARTED);
            fail("should have failed");
        } catch (final TooMuchAnnouncesFailedInARawException | AnnounceException ignore) {
        }
        //noinspection ConstantConditions
        final LocalDateTime lastDateTime = announcer.getLastAnnouncedAt().get();
        assertThat(lastDateTime).isNotNull();

        // Ensure the next date won't be the same if the code runs too fast
        Thread.sleep(50);

        try {
            announcer.announce(RequestEvent.STARTED);
        } catch (final TooMuchAnnouncesFailedInARawException | AnnounceException ignore) {
            fail("should not have failed");
        }
        //noinspection ConstantConditions
        assertThat(announcer.getLastAnnouncedAt().get()).isAfter(lastDateTime);
    }

    @Test
    public void shouldUpdateLastPeersStatsAndIntevalOnEachAnnounce() throws AnnounceException {
        final MockedTorrent torrent = MockedTorrentTest.createOneMock("abcd");

        final TrackerClient trackerClient = mock(TrackerClient.class);

        doReturn(new SuccessAnnounceResponse(1800, 164, 12))
                .doThrow(new AnnounceException("yeah ! :)"))
                .doReturn(new SuccessAnnounceResponse(900, 180, 30))
                .when(trackerClient).announce(anyString(), any());
        final AnnounceDataAccessor dataAccessor = mock(AnnounceDataAccessor.class);
        doReturn("dd=ff&qq=d").when(dataAccessor).getHttpRequestQueryForTorrent(any(InfoHash.class), eq(RequestEvent.STARTED));
        doReturn(Lists.newArrayList()).when(dataAccessor).getHttpHeadersForTorrent();

        final Announcer announcer = new Announcer(torrent, dataAccessor, Mockito.mock(HttpClient.class));
        announcer.setTrackerClient(trackerClient);

        assertThat(announcer.getLastKnownInterval()).isEqualTo(5);
        assertThat(announcer.getLastKnownSeeders()).isEmpty();
        assertThat(announcer.getLastKnownLeechers()).isEmpty();
        try {
            announcer.announce(RequestEvent.STARTED);
        } catch (final TooMuchAnnouncesFailedInARawException | AnnounceException ignore) {
            fail("should not have failed");
        }
        assertThat(announcer.getLastKnownInterval()).isEqualTo(1800);
        assertThat(announcer.getLastKnownSeeders()).contains(164);
        assertThat(announcer.getLastKnownLeechers()).contains(12);

        try {
            announcer.announce(RequestEvent.STARTED);
            fail("should have failed");
        } catch (final TooMuchAnnouncesFailedInARawException | AnnounceException ignore) {
        }
        // same after a fail
        assertThat(announcer.getLastKnownInterval()).isEqualTo(1800);
        assertThat(announcer.getLastKnownSeeders()).contains(164);
        assertThat(announcer.getLastKnownLeechers()).contains(12);

        try {
            announcer.announce(RequestEvent.STARTED);
        } catch (final TooMuchAnnouncesFailedInARawException | AnnounceException ignore) {
            fail("should not have failed");
        }
        assertThat(announcer.getLastKnownInterval()).isEqualTo(900);
        assertThat(announcer.getLastKnownSeeders()).contains(180);
        assertThat(announcer.getLastKnownLeechers()).contains(30);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldBeEqualsByInfoHash() {
        final MockedTorrent torrent1 = MockedTorrentTest.createOneMock("abcd");
        final Announcer announcer1 = new Announcer(torrent1, null, Mockito.mock(HttpClient.class));


        final MockedTorrent torrent2 = MockedTorrentTest.createOneMock("abcd");
        final Announcer announcer2 = new Announcer(torrent2, null, Mockito.mock(HttpClient.class));

        assertThat(announcer1).isEqualTo(announcer2);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldNotBeEqualsWithDifferentInfoHash() {
        final MockedTorrent torrent1 = MockedTorrentTest.createOneMock("abcd");
        final Announcer announcer1 = new Announcer(torrent1, null, Mockito.mock(HttpClient.class));


        final MockedTorrent torrent2 = MockedTorrentTest.createOneMock("abcdefgh");
        final Announcer announcer2 = new Announcer(torrent2, null, Mockito.mock(HttpClient.class));

        assertThat(announcer1).isNotEqualTo(announcer2);
    }

}
