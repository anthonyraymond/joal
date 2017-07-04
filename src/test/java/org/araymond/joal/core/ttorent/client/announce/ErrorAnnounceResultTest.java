package org.araymond.joal.core.ttorent.client.announce;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 04/07/2017.
 */
public class ErrorAnnounceResultTest {

    @Test
    public void shouldNotBuildWithNullErrMessage() {
        assertThatThrownBy(() -> new AnnounceResult.ErrorAnnounceResult(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ErrMessage must not be null (may be empty)");
    }

    @Test
    public void shouldBuildWithEmptyErrMessage() {
        assertThat(new AnnounceResult.ErrorAnnounceResult("").getErrMessage()).isEqualTo("");
    }

    @Test
    public void shouldBuild() {
        final AnnounceResult.ErrorAnnounceResult announceResult = new AnnounceResult.ErrorAnnounceResult("this is an error");
        assertThat(announceResult.getErrMessage()).isEqualTo("this is an error");
        assertThat(announceResult.getType()).isEqualTo(AnnounceResult.AnnounceResultType.ERROR);
    }

}
