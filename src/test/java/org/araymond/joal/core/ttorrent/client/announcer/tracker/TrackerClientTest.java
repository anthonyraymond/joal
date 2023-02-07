package org.araymond.joal.core.ttorrent.client.announcer.tracker;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.http.HTTPAnnounceResponseMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerErrorMessage;
import org.apache.http.client.HttpClient;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TrackerClientTest {

    private HTTPAnnounceResponseMessage createMockedTrackerSuccessMessage() {
        final HTTPAnnounceResponseMessage message = mock(HTTPAnnounceResponseMessage.class);
        Mockito.doReturn(1800).when(message).getInterval();
        Mockito.doReturn(15).when(message).getIncomplete();
        Mockito.doReturn(132).when(message).getComplete();
        return message;
    }
    private HTTPAnnounceResponseMessage createMockedTrackerSuccessMessage(final int seeders) {
        final HTTPAnnounceResponseMessage message = mock(HTTPAnnounceResponseMessage.class);
        Mockito.doReturn(1800).when(message).getInterval();
        Mockito.doReturn(15).when(message).getIncomplete();
        Mockito.doReturn(seeders).when(message).getComplete();
        return message;
    }

    private TrackerMessage createMockedTrackerErrorMessage() {
        final HTTPTrackerErrorMessage message = mock(HTTPTrackerErrorMessage.class);
        Mockito.doReturn("expected error :)").when(message).getReason();
        return message;
    }

    private Iterable<Map.Entry<String, String>> createHeaders() {
        final List<Map.Entry<String, String>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>("headerKey", "headerValue"));
        return entries;
    }

    @Test
    public void shouldAlsoAcceptHttps() throws Exception {
        final TrackerClientUriProvider uriProvider = Mockito.spy(TrackerClientUriProviderTest.createOne("https://localhost"));

        final TrackerClient trackerClient = Mockito.spy(new TrackerClient(uriProvider, mock(TrackerResponseHandler.class), Mockito.mock(HttpClient.class)));
        Mockito.doReturn(
                this.createMockedTrackerSuccessMessage()
        ).when(trackerClient).makeCallAndGetResponseAsByteBuffer(any(URI.class), anyString(), any());

        trackerClient.announce("param=val&dd=q", this.createHeaders());

        Mockito.verify(uriProvider, never()).deleteCurrentAndMoveToNext();
    }

    @Test
    public void shouldThrowAnnounceExceptionAndMoveToNextUriWhenResponseIsError() throws Exception {
        final TrackerClientUriProvider uriProvider = Mockito.spy(TrackerClientUriProviderTest.createOne("http://localhost", "https://localhost"));

        final TrackerClient trackerClient = Mockito.spy(new TrackerClient(uriProvider, mock(TrackerResponseHandler.class), Mockito.mock(HttpClient.class)));
        Mockito.doReturn(
                this.createMockedTrackerErrorMessage()
        ).when(trackerClient).makeCallAndGetResponseAsByteBuffer(any(URI.class), anyString(), any());

        assertThatThrownBy(() -> trackerClient.announce("http://localhost", this.createHeaders()))
                .isInstanceOf(AnnounceException.class);

        Mockito.verify(uriProvider, never()).deleteCurrentAndMoveToNext();
        Mockito.verify(uriProvider, times(1)).moveToNext();
    }

    @Test
    public void shouldRemoveOneFromSeeders() throws Exception {
        // should remove one seeder because we are one of them
        final TrackerClientUriProvider uriProvider = Mockito.spy(TrackerClientUriProviderTest.createOne("https://localhost"));

        final TrackerClient trackerClient = Mockito.spy(new TrackerClient(uriProvider, mock(TrackerResponseHandler.class), Mockito.mock(HttpClient.class)));
        Mockito.doReturn(
                this.createMockedTrackerSuccessMessage()
        ).when(trackerClient).makeCallAndGetResponseAsByteBuffer(any(URI.class), anyString(), any());

        final SuccessAnnounceResponse announceResponse = trackerClient.announce("param=val&dd=q", this.createHeaders());

        final HTTPAnnounceResponseMessage expected = this.createMockedTrackerSuccessMessage();
        assertThat(announceResponse.getLeechers()).isEqualTo(expected.getIncomplete());
        assertThat(announceResponse.getSeeders()).isEqualTo(expected.getComplete() - 1);
        assertThat(announceResponse.getInterval()).isEqualTo(expected.getInterval());
    }

    @Test
    public void shouldNotFailToRemoveOneFromSeedersIfThereIsNoSeeders() throws Exception {
        // should remove one seeder because we are one of them
        final TrackerClientUriProvider uriProvider = Mockito.spy(TrackerClientUriProviderTest.createOne("https://localhost"));

        final TrackerClient trackerClient = Mockito.spy(new TrackerClient(uriProvider, mock(TrackerResponseHandler.class), Mockito.mock(HttpClient.class)));
        Mockito.doReturn(
                this.createMockedTrackerSuccessMessage(0)
        ).when(trackerClient).makeCallAndGetResponseAsByteBuffer(any(URI.class), anyString(), any());

        final SuccessAnnounceResponse announceResponse = trackerClient.announce("param=val&dd=q", this.createHeaders());

        final HTTPAnnounceResponseMessage expected = this.createMockedTrackerSuccessMessage();
        assertThat(announceResponse.getLeechers()).isEqualTo(expected.getIncomplete());
        assertThat(announceResponse.getSeeders()).isEqualTo(0);
        assertThat(announceResponse.getInterval()).isEqualTo(expected.getInterval());
    }

}
