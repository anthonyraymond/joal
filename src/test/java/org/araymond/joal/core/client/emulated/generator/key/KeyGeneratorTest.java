package org.araymond.joal.core.client.emulated.generator.key;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.HashKeyAlgorithm;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class KeyGeneratorTest {

    public static KeyGenerator createDefault() {
        return new NeverRefreshKeyGenerator(new HashKeyAlgorithm(8), Casing.NONE);
    }

    @Test
    public void shouldNotBuildWithNullAlgorithm() {
        assertThatThrownBy(() -> new DefaultKeyGenerator(null, Casing.NONE))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessage("key algorithm must not be null.");
    }


    @Test
    public void shouldGenerateKeyLowerCased() {
        final KeyGenerator generator = new DefaultKeyGenerator(new HashKeyAlgorithm(8), Casing.LOWER);

        for (int i = 0; i < 30; i++) {
            assertThat(generator.generateKey()).matches("[0-9a-z]{8}");
        }
    }

    @Test
    public void shouldGenerateKeyUpperCased() {
        final KeyGenerator generator = new DefaultKeyGenerator(new HashKeyAlgorithm(8), Casing.UPPER);

        for (int i = 0; i < 30; i++) {
            assertThat(generator.generateKey()).matches("[0-9A-Z]{8}");
        }
    }

    private static final class DefaultKeyGenerator extends KeyGenerator {

        private DefaultKeyGenerator(final KeyAlgorithm algorithm, final Casing keyCase) {
            super(algorithm, keyCase);
        }

        @Override
        public String getKey(final MockedTorrent torrent, final RequestEvent event) {
            return "";
        }
    }

}
