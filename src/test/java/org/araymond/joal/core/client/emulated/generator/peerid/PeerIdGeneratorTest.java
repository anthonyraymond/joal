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
        return new NeverRefreshPeerIdGenerator("-AA-", "[a-zA-Z]", false);
    }

    public static PeerIdGenerator createDefault(final String prefix) {
        return new NeverRefreshPeerIdGenerator(prefix, "[a-zA-Z]", false);
    }

    @Test
    public void shouldNotBuildWithNullPrefix() {
        assertThatThrownBy(() -> new DefaultPeerIdGenerator(null, "[a-zA-Z]", false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("prefix must not be null or empty.");
    }

    @Test
    public void shouldNotBuildWithEmptyPrefix() {
        assertThatThrownBy(() -> new DefaultPeerIdGenerator("  ", "[a-zA-Z]", false))
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
    public void shouldUrlEncodePeerId() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[\u0000\u0001]", true);

        assertThat(peerIdGenerator.generatePeerId()).contains("%");
    }

    @Test
    public void shouldNotFailToUrlEncodeIfThereIsNoSpecialChars() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[\u0000\u00010]", true);

        assertThat(peerIdGenerator.urlEncodeLowerCasedSpecialChars("AAAAAAAAAAAAAAAAAA")).isEqualTo("AAAAAAAAAAAAAAAAAA");
    }

    @Test
    public void shouldNotFailToUrlEncodeIfThereIsOnlySpecialChars() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[\u0000\u00010]", true);

        assertThat(peerIdGenerator.urlEncodeLowerCasedSpecialChars("\u0010\u0010\u0010")).isEqualTo("%10%10%10");
    }

    @Test
    public void shouldLowerCaseOnlyEncodedCharsWhenUrlEncoding() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", "[\u0000\u00010]", true);

        assertThat(peerIdGenerator.urlEncodeLowerCasedSpecialChars("\u00a6\u00ccAa\u0012\u00ea")).isEqualTo("%a6%ccAa%12%ea");
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
