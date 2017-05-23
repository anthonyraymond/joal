package org.araymond.joal.core.client.emulated;

import org.junit.Test;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.KeyInfo;
import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.ValueType.ALPHABETIC;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 24/04/2017.
 */
public class KeyInfoTest {

    @Test
    public void shouldBuild() {
        final KeyInfo keyInfo = new KeyInfo(8, ALPHABETIC, false, true);
        assertThat(keyInfo.getLength()).isEqualTo(8);
        assertThat(keyInfo.getType()).isEqualTo(ALPHABETIC);
        assertThat(keyInfo.isUpperCase()).isFalse();
        assertThat(keyInfo.isLowerCase()).isTrue();
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final KeyInfo keyInfo = new KeyInfo(8, ALPHABETIC, false, true);
        final KeyInfo keyInfo2 = new KeyInfo(8, ALPHABETIC, false, true);
        assertThat(keyInfo).isEqualTo(keyInfo2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final KeyInfo keyInfo = new KeyInfo(8, ALPHABETIC, false, true);
        final KeyInfo keyInfo2 = new KeyInfo(8, ALPHABETIC, false, true);
        assertThat(keyInfo.hashCode()).isEqualTo(keyInfo2.hashCode());
    }

}
