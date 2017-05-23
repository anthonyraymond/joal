package org.araymond.joal.core.client.emulated;

import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.utils.MockedInjections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 25/04/2017.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BitTorrentClientProvider.class, JoalConfigProvider.class, MockedInjections.DefaultObjectMapperDI.class})
@TestPropertySource(properties = {
        "joal-conf=src/test/resources/configtest",
})
public class BitTorrentClientProviderDITest {

    @Inject
    private BitTorrentClientProvider clientProvider;

    @Test
    public void shouldInjectConfigProvider() throws FileNotFoundException {
        final BitTorrentClient almostExpected = BitTorrentClientConfigTest.defaultClientConfig.createClient();

        clientProvider.generateNewClient();
        final BitTorrentClient result = clientProvider.get();

        assertThat(result.getQuery()).isEqualTo(almostExpected.getQuery());
        assertThat(result.getNumwant()).isEqualTo(almostExpected.getNumwant());
        assertThat(result.getHeaders()).containsExactlyInAnyOrder(almostExpected.getHeaders().toArray(new Map.Entry[] {}));
        assertThat(result.getKey().get()).isNotEmpty();
        assertThat(result.getPeerId()).isNotEmpty();
    }

}
