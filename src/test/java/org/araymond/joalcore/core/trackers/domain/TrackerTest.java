package org.araymond.joalcore.core.trackers.domain;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class TrackerTest {

    public Instant inThirtyMinutes() {
        return Instant.now().plus(Duration.ofMinutes(30));
    }
    public Instant now() {
        return Instant.now();
    }

    private URL randomUrl() {
        try {
            var rand = new Random();
            return URI.create("http://a.%d.com/path".formatted(rand.nextInt())).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Tracker randomTracker() {
        return new Tracker();
    }

    @Test
    public void shouldRequireAnnounceOnCreation() {
        var tracker = randomTracker();

        assertThat(tracker.requireAnnounce(now())).isTrue();
    }

    @Test
    public void shouldNotRequireAnnounceAfterAnAnnounceWhileAResponseIsNotReceived() {
        var tracker = randomTracker();

        tracker.announce();
        assertThat(tracker.requireAnnounce(now())).isFalse();
        assertThat(tracker.requireAnnounce(inThirtyMinutes())).isFalse();

        tracker.announceSucceed(new AnnounceSucceed(Duration.ofMinutes(30)));
        assertThat(tracker.requireAnnounce(now())).isFalse();
        assertThat(tracker.requireAnnounce(inThirtyMinutes())).isTrue();
    }


    @Test
    public void shouldNotRequireAnnounceAfterAnAnnounceWhileAnErrorIsNotReceived() {
        var tracker = randomTracker();

        tracker.announce();
        assertThat(tracker.requireAnnounce(now())).isFalse();
        assertThat(tracker.requireAnnounce(inThirtyMinutes())).isFalse();

        tracker.announceFailed(new AnnounceFailed(), new AnnounceBackoffService.DefaultBackoffService());
        assertThat(tracker.requireAnnounce(now())).isFalse();
        assertThat(tracker.requireAnnounce(inThirtyMinutes())).isTrue();
    }

    // TODO: add disable method
    // TODO: impossible to announce while disabled
    // TODO: requireAnnounce returns False for disabled
    // TODO: make it not possible to announce while still awaiting answer (unless STOPPED)
    // TODO: make it possible to announce COMPLETED AND STOPPED even when nextAnnounce is not reached
    // TODO: test that the backoff is reset after a successful announce

}