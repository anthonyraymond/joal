package org.araymond.joal.core.client.emulated.generator.numwant;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 19/07/2017.
 */
public class NumwantProviderTest {

    @Test
    public void shouldNotBuildWithoutNumwant() {
        assertThatThrownBy(() -> new NumwantProvider(null, 0))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("numwant must not be null.");
    }

    @Test
    public void shouldNotBuildWithNumwantLessThanOne() {
        assertThatThrownBy(() -> new NumwantProvider(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("numwant must be at least 1.");
    }

    @Test
    public void shouldBuildWithNumwantEqualsOne() {
        final NumwantProvider numwantProvider = new NumwantProvider(1, 0);

        assertThat(numwantProvider.get(RequestEvent.STARTED)).isEqualTo(1);
    }

    @Test
    public void shouldNotBuildWithoutNumwantOnStop() {
        assertThatThrownBy(() -> new NumwantProvider(200, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("numwantOnStop must not be null.");
    }

    @Test
    public void shouldNotBuildWithNumwantOnStopLessThanZero() {
        assertThatThrownBy(() -> new NumwantProvider(200, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("numwantOnStop must be at least 0.");
    }

    @Test
    public void shouldBuildWithNumwantOnStopEqualsZero() {
        final NumwantProvider numwantProvider = new NumwantProvider(200, 0);

        assertThat(numwantProvider.get(RequestEvent.STOPPED)).isEqualTo(0);
    }

    @Test
    public void shouldBuild() {
        final NumwantProvider numwantProvider = new NumwantProvider(200, 0);

        assertThat(numwantProvider.get(RequestEvent.STARTED)).isEqualTo(200);
        assertThat(numwantProvider.get(RequestEvent.STOPPED)).isEqualTo(0);
    }

}
