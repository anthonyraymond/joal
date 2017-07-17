package org.araymond.joal.core.client.emulated;

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
        assertThatThrownBy(() -> new BitTorrentClient(null, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close"))))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("peerIdGenerator cannot be null or empty");
    }

    @Test
    public void shouldBuildAndReturnOptionalEmptyIfKeyIsNull() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, null, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")));

        assertThat(client.getKey(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED)).isEmpty();
    }

    @Test
    public void shouldNotBuildIfQueryIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, null, Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query cannot be null or empty");
    }

    @Test
    public void shouldNotBuildIfQueryIsEmpty() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "     ", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query cannot be null or empty");
    }

    @Test
    public void shouldNotBuildIfHeadersIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("headers cannot be null");
    }

    @Test
    public void shouldBuildIfHeadersIsEmpty() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.emptyList());

        assertThat(client.getHeaders()).isEmpty();
    }


    @Test
    public void shouldBuild() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")));
        assertThat(client.getPeerId(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED)).isEqualTo(defaultPeerIdGenerator.getPeerId(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED));
        assertThat(client.getKey(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED).get()).isEqualTo(defaultKeyGenerator.getKey(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED));
        assertThat(client.getQuery()).isEqualTo("myqueryString");
        assertThat(client.getHeaders()).hasSize(1);
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final BitTorrentClient client = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")));
        final BitTorrentClient client2 = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")));
        assertThat(client).isEqualTo(client2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final BitTorrentClient client = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")));
        final BitTorrentClient client2 = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), "myqueryString", Collections.singletonList(new BitTorrentClientConfig.HttpHeader("Connection", "close")));
        assertThat(client.hashCode()).isEqualTo(client2.hashCode());
    }

}
