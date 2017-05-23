package org.araymond.joal.core.client.emulated;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 26/04/2017.
 */
public class BitTorrentClientTest {

    @Test
    public void shouldNotBuildIfPeerIdIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(null, "qs5d4qs5d6", "myqueryString", Arrays.asList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("peerId cannot be null or empty");
    }

    @Test
    public void shouldNotBuildIfPeerIdIsEmpty() {
        assertThatThrownBy(() -> new BitTorrentClient("   ", "qs5d4qs5d6", "myqueryString", Arrays.asList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("peerId cannot be null or empty");
    }

    @Test
    public void shouldBuildAndReturnOptionalEmptyIfKeyIsNull() {
        final BitTorrentClient client = new BitTorrentClient("-azqsd-332153", null, "myqueryString", Arrays.asList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200);

        assertThat(client.getKey()).isEmpty();
    }

    @Test
    public void shouldNotBuildIfKeyIsEmpty() {
        assertThatThrownBy(() -> new BitTorrentClient("-azqsd-332153", "   ", "myqueryString", Arrays.asList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key can be null but must not be empty");
    }

    @Test
    public void shouldNotBuildIfQueryIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", null, Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query cannot be null or empty");
    }

    @Test
    public void shouldNotBuildIfQueryIsEmpty() {
        assertThatThrownBy(() -> new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "     ", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query cannot be null or empty");
    }

    @Test
    public void shouldNotBuildIfHeadersIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", null, 200))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("headers cannot be null");
    }

    @Test
    public void shouldBuildIfHeadersIsEmpty() {
        final BitTorrentClient client = new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Collections.emptyList(), 200);

        assertThat(client.getHeaders()).isEmpty();
    }

    @Test
    public void shouldNotBuildIfNumwantIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("numwant cannot be null");
    }

    @Test
    public void shouldNotBuildIfNumwantIsLessThanOne() {
        assertThatThrownBy(() -> new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("numwant must be greater than 0");
    }

    @Test
    public void shouldBuildIfNumwantIsOne() {
        final BitTorrentClient client = new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 1);

        assertThat(client.getNumwant()).isEqualTo(1);
    }

    @Test
    public void shouldBuild() {
        final BitTorrentClient client = new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200);
        assertThat(client.getPeerId()).isEqualTo("-azqsd-332153");
        assertThat(client.getKey().get()).isEqualTo("qs5d4qs5d6");
        assertThat(client.getQuery()).isEqualTo("myqueryString");
        assertThat(client.getHeaders()).hasSize(1);
        assertThat(client.getNumwant()).isEqualTo(200);
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final BitTorrentClient client = new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Arrays.asList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200);
        final BitTorrentClient client2 = new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Arrays.asList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200);
        assertThat(client).isEqualTo(client2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final BitTorrentClient client = new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Arrays.asList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200);
        final BitTorrentClient client2 = new BitTorrentClient("-azqsd-332153", "qs5d4qs5d6", "myqueryString", Arrays.asList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200);
        assertThat(client.hashCode()).isEqualTo(client2.hashCode());
    }

}
