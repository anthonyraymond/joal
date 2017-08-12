package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class PeerIdGeneratorTest {

    public static PeerIdGenerator createDefault() {
        return new NeverRefreshPeerIdGenerator("-AA-", "[a-zA-Z]{16}", false);
    }
    public static PeerIdGenerator createForPattern(final String pattern, final boolean isUrlEncoded) {
        return new NeverRefreshPeerIdGenerator("-AA-", pattern, isUrlEncoded);
    }

    @Test
    public void shouldNotBuildWithNullPrefix() {
        assertThatThrownBy(() -> new DefaultPeerIdGenerator(null, "[a-zA-Z]{16}", false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("prefix must not be null or empty.");
    }

    @Test
    public void shouldNotBuildWithEmptyPrefix() {
        assertThatThrownBy(() -> new DefaultPeerIdGenerator("  ", "[a-zA-Z]{16}", false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("prefix must not be null or empty.");
    }

    @Test
    public void shouldNotBuildWithoutTypePrefix() {
        assertThatThrownBy(() -> new DefaultPeerIdGenerator("-my.pre-", null, false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("peerId pattern must not be null or empty.");
    }

    @Test
    public void shouldGeneratePeerIdWithProperLength() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[a-zA-Z]{12}", false);

        for (int i = 0; i < 30; i++) {
            assertThat(peerIdGenerator.generatePeerId())
                    .startsWith("-my.pre-")
                    .hasSize(PeerIdGenerator.PEER_ID_LENGTH);
        }
    }

    @Test
    public void shouldGeneratePeerIdAndBeUpperCase() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[A-Z]", false);

        for (int i = 0; i < 30; i++) {
            assertThat(peerIdGenerator.generatePeerId())
                    .startsWith("-my.pre-")
                    .matches("-my.pre-[A-Z]+");
        }
    }

    @Test
    public void shouldGeneratePeerIdAndBeLowerCase() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[a-z]", false);

        for (int i = 0; i < 30; i++) {
            assertThat(peerIdGenerator.generatePeerId())
                    .startsWith("-my.pre-")
                    .matches("-my.pre-[a-z]+");
        }
    }

    @Test
    public void shouldBuild() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[a-zA-Z]", false);
        assertThat(peerIdGenerator.getPrefix()).isEqualTo("-my.pre-");
        assertThat(peerIdGenerator.getPattern()).isEqualTo("[a-zA-Z]");
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[a-zA-Z]", false);
        final PeerIdGenerator peerIdGenerator2 = new DefaultPeerIdGenerator("-my.pre-", "[a-zA-Z]", false);
        assertThat(peerIdGenerator).isEqualTo(peerIdGenerator2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[a-zA-Z]", false);
        final PeerIdGenerator peerIdGenerator2 = new DefaultPeerIdGenerator("-my.pre-", "[a-zA-Z]", false);
        assertThat(peerIdGenerator.hashCode()).isEqualTo(peerIdGenerator2.hashCode());
    }
    
    private static class DefaultPeerIdGenerator extends PeerIdGenerator {

        protected DefaultPeerIdGenerator(final String prefix, final String pattern, final boolean isUrlEncoded) {
            super(prefix, pattern, isUrlEncoded);
        }

        @Override
        public String getPeerId(final MockedTorrent torrent, final TrackerMessage.AnnounceRequestMessage.RequestEvent event) {
            return "";
        }
    }

}
