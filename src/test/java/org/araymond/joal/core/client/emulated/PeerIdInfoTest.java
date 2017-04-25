package org.araymond.joal.core.client.emulated;

import org.junit.Test;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.*;
import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.ValueType.ALPHABETIC;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 24/04/2017.
 */
public class PeerIdInfoTest {

    @Test
    public void shouldGeneratePeerIdWithProperLength() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-my.pre-", ALPHABETIC, false, false);

        assertThat(peerIdInfo.generateNewPeerId())
            .startsWith("-my.pre-")
            .hasSize(PeerIdInfo.PEER_ID_LENGTH);
    }

    @Test
    public void shouldGeneratePeerIdAndBeUpperCase() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-my.pre-", ALPHABETIC, true, false);

        assertThat(peerIdInfo.generateNewPeerId())
                .startsWith("-my.pre-")
                .matches("-my.pre-[A-Z]+");
    }

    @Test
    public void shouldGeneratePeerIdAndBeLowerCase() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-my.pre-", ALPHABETIC, false, true);

        assertThat(peerIdInfo.generateNewPeerId())
                .startsWith("-my.pre-")
                .matches("-my.pre-[a-z]+");
    }

    @Test
    public void shouldBuild() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-my.pre-", ALPHABETIC, false, true);
        assertThat(peerIdInfo.getPrefix()).isEqualTo("-my.pre-");
        assertThat(peerIdInfo.getType()).isEqualTo(ALPHABETIC);
        assertThat(peerIdInfo.isUpperCase()).isFalse();
        assertThat(peerIdInfo.isLowerCase()).isTrue();
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-my.pre-", ALPHABETIC, false, true);
        final PeerIdInfo peerIdInfo2 = new PeerIdInfo("-my.pre-", ALPHABETIC, false, true);
        assertThat(peerIdInfo).isEqualTo(peerIdInfo2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-my.pre-", ALPHABETIC, false, true);
        final PeerIdInfo peerIdInfo2 = new PeerIdInfo("-my.pre-", ALPHABETIC, false, true);
        assertThat(peerIdInfo.hashCode()).isEqualTo(peerIdInfo2.hashCode());
    }

}
