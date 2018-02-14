package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnounceResponseHandlerChainTest {

    @Test
    public void shouldPlayHandlerInInsertionOrder() {
        final AnnounceResponseHandlerChain chain = new AnnounceResponseHandlerChain();

        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            final int handlerPosition = i;
            chain.appendHandler(new DefaultResponseHandler() {
                @Override
                public void onAnnouncerWillAnnounce(final Announcer announcer, TrackerMessage.AnnounceRequestMessage.RequestEvent event) {
                    builder.append(handlerPosition);
                }
            });
        }

        for (int i = 0; i < 50; i++) {
            chain.onAnnounceWillAnnounce(null, null);
        }

        assertThat(builder.toString()).matches("(0123456789){50}");
    }


    private static class DefaultResponseHandler implements AnnounceResponseHandlerChainElement {
        @Override
        public void onAnnouncerWillAnnounce(final Announcer announcer, TrackerMessage.AnnounceRequestMessage.RequestEvent event) {
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
        public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        }
    }


}
