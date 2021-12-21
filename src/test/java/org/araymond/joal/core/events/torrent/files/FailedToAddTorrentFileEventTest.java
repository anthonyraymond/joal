package org.araymond.joal.core.events.torrent.files;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FailedToAddTorrentFileEventTest {

    @Test
    public void shouldBuild() {
        final FailedToAddTorrentFileEvent event = new FailedToAddTorrentFileEvent("dd", "err");

        assertThat(event.getFileName()).isEqualTo("dd");
        assertThat(event.getError()).isEqualTo("err");
    }

}
