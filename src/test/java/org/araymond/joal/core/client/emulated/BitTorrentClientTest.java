package org.araymond.joal.core.client.emulated;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.key.KeyGeneratorTest;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGeneratorTest;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 26/04/2017.
 */
public class BitTorrentClientTest {

    private final KeyGenerator defaultKeyGenerator = KeyGeneratorTest.createDefault();
    private final PeerIdGenerator defaultPeerIdGenerator = PeerIdGeneratorTest.createDefault();

    @Test
    public void shouldNotBuildIfPeerIdIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(null, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("peerIdGenerator cannot be null or empty");
    }

    @Test
    public void shouldBuildAndReturnOptionalEmptyIfKeyIsNull() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, null, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0);

        assertThat(client.getKey(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED)).isEmpty();
    }

    @Test
    public void shouldNotBuildIfQueryIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, null, Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query cannot be null or empty");
    }

    @Test
    public void shouldNotBuildIfQueryIsEmpty() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "     ", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query cannot be null or empty");
    }

    @Test
    public void shouldNotBuildIfHeadersIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", null, 200, 0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("headers cannot be null");
    }

    @Test
    public void shouldBuildIfHeadersIsEmpty() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.emptyList(), 200, 0);

        assertThat(client.getHeaders()).isEmpty();
    }

    @Test
    public void shouldNotBuildIfNumwantIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), null, 0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("numwant cannot be null");
    }

    @Test
    public void shouldNotBuildIfNumwantIsLessThanOne() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("numwant must be greater than 0");
    }

    @Test
    public void shouldBuildIfNumwantIsOne() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 1, 0);

        assertThat(client.getNumwant()).isEqualTo(1);
    }

    @Test
    public void shouldNotBuildIfNumwantOnStopIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("numwantOnStop cannot be null");
    }

    @Test
    public void shouldNotBuildIfNumwantOnStopIsLessThanZero() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("numwantOnStop must be at least 0");
    }

    @Test
    public void shouldBuildIfNumwantOnStopIsOne() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 1);

        assertThat(client.getNumwantOnStop()).isEqualTo(1);
    }

    @Test
    public void shouldBuild() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0);
        assertThat(client.getPeerId(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED)).isEqualTo(defaultPeerIdGenerator.getPeerId(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED));
        assertThat(client.getKey(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED).get()).isEqualTo(defaultKeyGenerator.getKey(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED));
        assertThat(client.getQuery()).isEqualTo("myqueryString");
        assertThat(client.getHeaders()).hasSize(1);
        assertThat(client.getNumwant()).isEqualTo(200);
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final BitTorrentClient client = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0);
        final BitTorrentClient client2 = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0);
        assertThat(client).isEqualTo(client2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final BitTorrentClient client = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0);
        final BitTorrentClient client2 = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")), 200, 0);
        assertThat(client.hashCode()).isEqualTo(client2.hashCode());
    }

}
