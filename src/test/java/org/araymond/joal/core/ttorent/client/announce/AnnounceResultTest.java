package org.araymond.joal.core.ttorent.client.announce;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.araymond.joal.core.ttorent.client.announce.AnnounceResult.AnnounceResultType.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 04/07/2017.
 */
public class AnnounceResultTest {

    @Test
    public void shouldNotBuildWithoutType() {
        assertThatThrownBy(() -> new DefaultAnnounceResult(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("AnnounceResultType must not be null");
    }

    @Test
    public void shouldBuild() {
        final LocalDateTime current = LocalDateTime.now().minusNanos(1);
        final DefaultAnnounceResult announceResult = new DefaultAnnounceResult(SUCCESS);

        assertThat(announceResult.getType()).isEqualTo(SUCCESS);
        assertThat(announceResult.getDateTime())
                .isAfter(current)
                .isBefore(current.plusMinutes(1L));
    }


    private static class DefaultAnnounceResult extends AnnounceResult {
        private DefaultAnnounceResult(final AnnounceResult.AnnounceResultType type) {
            super(type);
        }

    }

}
