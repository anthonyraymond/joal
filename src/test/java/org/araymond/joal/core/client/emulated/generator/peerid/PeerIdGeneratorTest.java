package org.araymond.joal.core.client.emulated.generator.peerid;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.StringTypes;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;

import static org.araymond.joal.core.client.emulated.generator.StringTypes.ALPHABETIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class PeerIdGeneratorTest {

    public static PeerIdGenerator createDefault() {
        return new NeverRefreshPeerIdGenerator("-AA-", StringTypes.ALPHABETIC, false, false);
    }

    @Test
    public void shouldNotBuildWithNullPrefix() {
        assertThatThrownBy(() -> new DefaultPeerIdGenerator(null, ALPHABETIC, false, false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("prefix must not be null or empty.");
    }

    @Test
    public void shouldNotBuildWithEmptyPrefix() {
        assertThatThrownBy(() -> new DefaultPeerIdGenerator("  ", ALPHABETIC, false, false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("prefix must not be null or empty.");
    }

    @Test
    public void shouldNotBuildWithoutTypePrefix() {
        assertThatThrownBy(() -> new DefaultPeerIdGenerator("-my.pre-", null, false, false))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("peerId type must not be null.");
    }

    @Test
    public void shouldGeneratePeerIdWithProperLength() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", ALPHABETIC, false, false);

        for (int i = 0; i < 30; i++) {
            assertThat(peerIdGenerator.generatePeerId())
                    .startsWith("-my.pre-")
                    .hasSize(PeerIdGenerator.PEER_ID_LENGTH);
        }
    }

    @Test
    public void shouldGeneratePeerIdAndBeUpperCase() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", ALPHABETIC, true, false);

        for (int i = 0; i < 30; i++) {
            assertThat(peerIdGenerator.generatePeerId())
                    .startsWith("-my.pre-")
                    .matches("-my.pre-[A-Z]+");
        }
    }

    @Test
    public void shouldGeneratePeerIdAndBeLowerCase() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", ALPHABETIC, false, true);

        for (int i = 0; i < 30; i++) {
            assertThat(peerIdGenerator.generatePeerId())
                    .startsWith("-my.pre-")
                    .matches("-my.pre-[a-z]+");
        }
    }

    @Test
    public void shouldBuild() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", ALPHABETIC, false, true);
        assertThat(peerIdGenerator.getPrefix()).isEqualTo("-my.pre-");
        assertThat(peerIdGenerator.getType()).isEqualTo(ALPHABETIC);
        assertThat(peerIdGenerator.isUpperCase()).isFalse();
        assertThat(peerIdGenerator.isLowerCase()).isTrue();
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", ALPHABETIC, false, true);
        final PeerIdGenerator peerIdGenerator2 = new DefaultPeerIdGenerator("-my.pre-", ALPHABETIC, false, true);
        assertThat(peerIdGenerator).isEqualTo(peerIdGenerator2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final PeerIdGenerator peerIdGenerator = new DefaultPeerIdGenerator("-my.pre-", ALPHABETIC, false, true);
        final PeerIdGenerator peerIdGenerator2 = new DefaultPeerIdGenerator("-my.pre-", ALPHABETIC, false, true);
        assertThat(peerIdGenerator.hashCode()).isEqualTo(peerIdGenerator2.hashCode());
    }
    
    private static class DefaultPeerIdGenerator extends PeerIdGenerator {

        protected DefaultPeerIdGenerator(final String prefix, final StringTypes type, final boolean upperCase, final boolean lowerCase) {
            super(prefix, type, upperCase, lowerCase);
        }

        @Override
        public String getPeerId(final MockedTorrent torrent, final TrackerMessage.AnnounceRequestMessage.RequestEvent event) {
            return "";
        }
    }

}
