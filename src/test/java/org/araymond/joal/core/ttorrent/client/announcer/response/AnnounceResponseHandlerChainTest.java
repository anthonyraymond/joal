package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class AnnounceResponseHandlerChainTest {

    @Test
    public void shouldPlayHandlerInInsertionOrder() {
        final AnnounceResponseHandlerChain chain = new AnnounceResponseHandlerChain();

        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            final int handlerPosition = i;
            chain.appendHandler(new DefaultResponseHandler() {
                @Override
                public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
                    builder.append(handlerPosition);
                }
            });
        }

        for (int i = 0; i < 50; i++) {
            chain.onAnnounceWillAnnounce(null, null);
        }

        assertThat(builder.toString()).matches("(0123456789){50}");
    }

    @Test
    public void shouldDispatchOnWillAnnounce() {
        final AnnounceResponseHandler e1 = mock(AnnounceResponseHandler.class);
        final AnnounceResponseHandler e2 = mock(AnnounceResponseHandler.class);

        final AnnounceResponseHandlerChain announceResponseHandlerChain = new AnnounceResponseHandlerChain();
        announceResponseHandlerChain.appendHandler(e1);
        announceResponseHandlerChain.appendHandler(e2);

        final Announcer announcer = mock(Announcer.class);

        announceResponseHandlerChain.onAnnounceWillAnnounce(RequestEvent.STARTED, announcer);

        Mockito.verify(e1, times(1)).onAnnouncerWillAnnounce(eq(announcer), eq(RequestEvent.STARTED));
        Mockito.verify(e2, times(1)).onAnnouncerWillAnnounce(eq(announcer), eq(RequestEvent.STARTED));
        Mockito.verifyNoMoreInteractions(e1, e2);
    }

    @Test
    public void shouldDispatchSuccessAnnounceStart() {
        final AnnounceResponseHandler e1 = mock(AnnounceResponseHandler.class);
        final AnnounceResponseHandler e2 = mock(AnnounceResponseHandler.class);

        final AnnounceResponseHandlerChain announceResponseHandlerChain = new AnnounceResponseHandlerChain();
        announceResponseHandlerChain.appendHandler(e1);
        announceResponseHandlerChain.appendHandler(e2);

        final Announcer announcer = mock(Announcer.class);
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);

        announceResponseHandlerChain.onAnnounceSuccess(RequestEvent.STARTED, announcer, successAnnounceResponse);

        Mockito.verify(e1, times(1)).onAnnounceStartSuccess(eq(announcer), eq(successAnnounceResponse));
        Mockito.verify(e2, times(1)).onAnnounceStartSuccess(eq(announcer), eq(successAnnounceResponse));
        Mockito.verifyNoMoreInteractions(e1, e2);
    }

    @Test
    public void shouldDispatchSuccessAnnounceRegular() {
        final AnnounceResponseHandler e1 = mock(AnnounceResponseHandler.class);
        final AnnounceResponseHandler e2 = mock(AnnounceResponseHandler.class);

        final AnnounceResponseHandlerChain announceResponseHandlerChain = new AnnounceResponseHandlerChain();
        announceResponseHandlerChain.appendHandler(e1);
        announceResponseHandlerChain.appendHandler(e2);

        final Announcer announcer = mock(Announcer.class);
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);

        announceResponseHandlerChain.onAnnounceSuccess(RequestEvent.NONE, announcer, successAnnounceResponse);

        Mockito.verify(e1, times(1)).onAnnounceRegularSuccess(eq(announcer), eq(successAnnounceResponse));
        Mockito.verify(e2, times(1)).onAnnounceRegularSuccess(eq(announcer), eq(successAnnounceResponse));
        Mockito.verifyNoMoreInteractions(e1, e2);
    }

    @Test
    public void shouldDispatchSuccessAnnounceStop() {
        final AnnounceResponseHandler e1 = mock(AnnounceResponseHandler.class);
        final AnnounceResponseHandler e2 = mock(AnnounceResponseHandler.class);

        final AnnounceResponseHandlerChain announceResponseHandlerChain = new AnnounceResponseHandlerChain();
        announceResponseHandlerChain.appendHandler(e1);
        announceResponseHandlerChain.appendHandler(e2);

        final Announcer announcer = mock(Announcer.class);
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);

        announceResponseHandlerChain.onAnnounceSuccess(RequestEvent.STOPPED, announcer, successAnnounceResponse);

        Mockito.verify(e1, times(1)).onAnnounceStopSuccess(eq(announcer), eq(successAnnounceResponse));
        Mockito.verify(e2, times(1)).onAnnounceStopSuccess(eq(announcer), eq(successAnnounceResponse));
        Mockito.verifyNoMoreInteractions(e1, e2);
    }

    @Test
    public void shouldDispatchFailAnnounceStart() {
        final AnnounceResponseHandler e1 = mock(AnnounceResponseHandler.class);
        final AnnounceResponseHandler e2 = mock(AnnounceResponseHandler.class);

        final AnnounceResponseHandlerChain announceResponseHandlerChain = new AnnounceResponseHandlerChain();
        announceResponseHandlerChain.appendHandler(e1);
        announceResponseHandlerChain.appendHandler(e2);

        final Announcer announcer = mock(Announcer.class);

        announceResponseHandlerChain.onAnnounceFailure(RequestEvent.STARTED, announcer, new Exception("oops"));

        Mockito.verify(e1, times(1)).onAnnounceStartFails(eq(announcer), any(Exception.class));
        Mockito.verify(e2, times(1)).onAnnounceStartFails(eq(announcer), any(Exception.class));
        Mockito.verifyNoMoreInteractions(e1, e2);
    }

    @Test
    public void shouldDispatchFailAnnounceRegular() {
        final AnnounceResponseHandler e1 = mock(AnnounceResponseHandler.class);
        final AnnounceResponseHandler e2 = mock(AnnounceResponseHandler.class);

        final AnnounceResponseHandlerChain announceResponseHandlerChain = new AnnounceResponseHandlerChain();
        announceResponseHandlerChain.appendHandler(e1);
        announceResponseHandlerChain.appendHandler(e2);

        final Announcer announcer = mock(Announcer.class);

        announceResponseHandlerChain.onAnnounceFailure(RequestEvent.NONE, announcer, new Exception("oops"));

        Mockito.verify(e1, times(1)).onAnnounceRegularFails(eq(announcer), any(Exception.class));
        Mockito.verify(e2, times(1)).onAnnounceRegularFails(eq(announcer), any(Exception.class));
        Mockito.verifyNoMoreInteractions(e1, e2);
    }

    @Test
    public void shouldDispatchFailAnnounceStop() {
        final AnnounceResponseHandler e1 = mock(AnnounceResponseHandler.class);
        final AnnounceResponseHandler e2 = mock(AnnounceResponseHandler.class);

        final AnnounceResponseHandlerChain announceResponseHandlerChain = new AnnounceResponseHandlerChain();
        announceResponseHandlerChain.appendHandler(e1);
        announceResponseHandlerChain.appendHandler(e2);

        final Announcer announcer = mock(Announcer.class);

        announceResponseHandlerChain.onAnnounceFailure(RequestEvent.STOPPED, announcer, new Exception("oops"));

        Mockito.verify(e1, times(1)).onAnnounceStopFails(eq(announcer), any(Exception.class));
        Mockito.verify(e2, times(1)).onAnnounceStopFails(eq(announcer), any(Exception.class));
        Mockito.verifyNoMoreInteractions(e1, e2);
    }

    @Test
    public void shouldDispatchTooManyFails() {
        final AnnounceResponseHandler e1 = mock(AnnounceResponseHandler.class);
        final AnnounceResponseHandler e2 = mock(AnnounceResponseHandler.class);

        final AnnounceResponseHandlerChain announceResponseHandlerChain = new AnnounceResponseHandlerChain();
        announceResponseHandlerChain.appendHandler(e1);
        announceResponseHandlerChain.appendHandler(e2);

        final Announcer announcer = mock(Announcer.class);

        announceResponseHandlerChain.onTooManyAnnounceFailedInARow(RequestEvent.STARTED, announcer, new TooManyAnnouncesFailedInARowException(mock(MockedTorrent.class)));

        Mockito.verify(e1, times(1)).onTooManyAnnounceFailedInARow(eq(announcer), any(TooManyAnnouncesFailedInARowException.class));
        Mockito.verify(e2, times(1)).onTooManyAnnounceFailedInARow(eq(announcer), any(TooManyAnnouncesFailedInARowException.class));
        Mockito.verifyNoMoreInteractions(e1, e2);
    }


    private static class DefaultResponseHandler implements AnnounceResponseHandler {
        @Override
        public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
        }

        @Override
        public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        }

        @Override
        public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
        }

        @Override
        public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        }

        @Override
        public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
        }

        @Override
        public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        }

        @Override
        public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
        }

        @Override
        public void onTooManyAnnounceFailedInARow(final Announcer announcer, final TooManyAnnouncesFailedInARowException e) {
        }
    }


}
