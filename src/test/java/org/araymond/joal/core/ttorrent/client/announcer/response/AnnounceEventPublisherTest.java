package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.events.announce.TooManyAnnouncesFailedEvent;
import org.araymond.joal.core.events.announce.WillAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class AnnounceEventPublisherTest {

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldPublishWillAnnounce() {
        final ApplicationEventPublisher appEventPublisher = mock(ApplicationEventPublisher.class);

        final AnnounceEventPublisher notifier = new AnnounceEventPublisher(appEventPublisher);
        final Announcer announcer = mock(Announcer.class);

        notifier.onAnnouncerWillAnnounce(announcer, RequestEvent.STARTED);

        final ArgumentCaptor<WillAnnounceEvent> captor = ArgumentCaptor.forClass(WillAnnounceEvent.class);

        Mockito.verify(appEventPublisher, times(1)).publishEvent(captor.capture());
        Mockito.verifyNoMoreInteractions(appEventPublisher);
        assertThat(captor.getValue()).isInstanceOf(WillAnnounceEvent.class);
        assertThat(captor.getValue().getAnnouncerFacade()).isSameAs(announcer);
        assertThat(captor.getValue().getEvent()).isSameAs(RequestEvent.STARTED);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldPublishStartSuccess() {
        final ApplicationEventPublisher appEventPublisher = mock(ApplicationEventPublisher.class);

        final AnnounceEventPublisher notifier = new AnnounceEventPublisher(appEventPublisher);
        final Announcer announcer = mock(Announcer.class);
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        notifier.onAnnounceStartSuccess(announcer, successAnnounceResponse);

        final ArgumentCaptor<SuccessfullyAnnounceEvent> captor = ArgumentCaptor.forClass(SuccessfullyAnnounceEvent.class);

        Mockito.verify(appEventPublisher, times(1)).publishEvent(captor.capture());
        Mockito.verifyNoMoreInteractions(appEventPublisher);
        assertThat(captor.getValue()).isInstanceOf(SuccessfullyAnnounceEvent.class);
        assertThat(captor.getValue().getAnnouncerFacade()).isSameAs(announcer);
        assertThat(captor.getValue().getEvent()).isSameAs(RequestEvent.STARTED);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldPublishStartFails() {
        final ApplicationEventPublisher appEventPublisher = mock(ApplicationEventPublisher.class);

        final AnnounceEventPublisher notifier = new AnnounceEventPublisher(appEventPublisher);
        final Announcer announcer = mock(Announcer.class);
        notifier.onAnnounceStartFails(announcer, new Exception("oops"));

        final ArgumentCaptor<FailedToAnnounceEvent> captor = ArgumentCaptor.forClass(FailedToAnnounceEvent.class);

        Mockito.verify(appEventPublisher, times(1)).publishEvent(captor.capture());
        Mockito.verifyNoMoreInteractions(appEventPublisher);
        assertThat(captor.getValue()).isInstanceOf(FailedToAnnounceEvent.class);
        assertThat(captor.getValue().getAnnouncerFacade()).isSameAs(announcer);
        assertThat(captor.getValue().getEvent()).isSameAs(RequestEvent.STARTED);
        assertThat(captor.getValue().getErrMessage()).isEqualTo("oops");
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldPublishRegularSuccess() {
        final ApplicationEventPublisher appEventPublisher = mock(ApplicationEventPublisher.class);

        final AnnounceEventPublisher notifier = new AnnounceEventPublisher(appEventPublisher);
        final Announcer announcer = mock(Announcer.class);
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        notifier.onAnnounceRegularSuccess(announcer, successAnnounceResponse);

        final ArgumentCaptor<SuccessfullyAnnounceEvent> captor = ArgumentCaptor.forClass(SuccessfullyAnnounceEvent.class);

        Mockito.verify(appEventPublisher, times(1)).publishEvent(captor.capture());
        Mockito.verifyNoMoreInteractions(appEventPublisher);
        assertThat(captor.getValue()).isInstanceOf(SuccessfullyAnnounceEvent.class);
        assertThat(captor.getValue().getAnnouncerFacade()).isSameAs(announcer);
        assertThat(captor.getValue().getEvent()).isSameAs(RequestEvent.NONE);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldPublishRegularFails() {
        final ApplicationEventPublisher appEventPublisher = mock(ApplicationEventPublisher.class);

        final AnnounceEventPublisher notifier = new AnnounceEventPublisher(appEventPublisher);
        final Announcer announcer = mock(Announcer.class);
        notifier.onAnnounceRegularFails(announcer, new Exception("oops"));

        final ArgumentCaptor<FailedToAnnounceEvent> captor = ArgumentCaptor.forClass(FailedToAnnounceEvent.class);

        Mockito.verify(appEventPublisher, times(1)).publishEvent(captor.capture());
        Mockito.verifyNoMoreInteractions(appEventPublisher);
        assertThat(captor.getValue()).isInstanceOf(FailedToAnnounceEvent.class);
        assertThat(captor.getValue().getAnnouncerFacade()).isSameAs(announcer);
        assertThat(captor.getValue().getEvent()).isSameAs(RequestEvent.NONE);
        assertThat(captor.getValue().getErrMessage()).isEqualTo("oops");
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldPublishStopSuccess() {
        final ApplicationEventPublisher appEventPublisher = mock(ApplicationEventPublisher.class);

        final AnnounceEventPublisher notifier = new AnnounceEventPublisher(appEventPublisher);
        final Announcer announcer = mock(Announcer.class);
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        notifier.onAnnounceStopSuccess(announcer, successAnnounceResponse);

        final ArgumentCaptor<SuccessfullyAnnounceEvent> captor = ArgumentCaptor.forClass(SuccessfullyAnnounceEvent.class);

        Mockito.verify(appEventPublisher, times(1)).publishEvent(captor.capture());
        Mockito.verifyNoMoreInteractions(appEventPublisher);
        assertThat(captor.getValue()).isInstanceOf(SuccessfullyAnnounceEvent.class);
        assertThat(captor.getValue().getAnnouncerFacade()).isSameAs(announcer);
        assertThat(captor.getValue().getEvent()).isSameAs(RequestEvent.STOPPED);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldPublishStopFails() {
        final ApplicationEventPublisher appEventPublisher = mock(ApplicationEventPublisher.class);

        final AnnounceEventPublisher notifier = new AnnounceEventPublisher(appEventPublisher);
        final Announcer announcer = mock(Announcer.class);
        notifier.onAnnounceStopFails(announcer, new Exception("oops"));

        final ArgumentCaptor<FailedToAnnounceEvent> captor = ArgumentCaptor.forClass(FailedToAnnounceEvent.class);

        Mockito.verify(appEventPublisher, times(1)).publishEvent(captor.capture());
        Mockito.verifyNoMoreInteractions(appEventPublisher);
        assertThat(captor.getValue()).isInstanceOf(FailedToAnnounceEvent.class);
        assertThat(captor.getValue().getAnnouncerFacade()).isSameAs(announcer);
        assertThat(captor.getValue().getEvent()).isSameAs(RequestEvent.STOPPED);
        assertThat(captor.getValue().getErrMessage()).isEqualTo("oops");
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldTooManyFails() {
        final ApplicationEventPublisher appEventPublisher = mock(ApplicationEventPublisher.class);

        final AnnounceEventPublisher notifier = new AnnounceEventPublisher(appEventPublisher);
        final Announcer announcer = mock(Announcer.class);
        notifier.onTooManyAnnounceFailedInARaw(announcer, new TooMuchAnnouncesFailedInARawException(mock(MockedTorrent.class)));

        final ArgumentCaptor<TooManyAnnouncesFailedEvent> captor = ArgumentCaptor.forClass(TooManyAnnouncesFailedEvent.class);

        Mockito.verify(appEventPublisher, times(1)).publishEvent(captor.capture());
        Mockito.verifyNoMoreInteractions(appEventPublisher);
        assertThat(captor.getValue()).isInstanceOf(TooManyAnnouncesFailedEvent.class);
        assertThat(captor.getValue().getAnnouncerFacade()).isSameAs(announcer);
    }

}
