package org.araymond.joal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by raymo on 18/04/2017.
 */
public class JoalConfigProviderTest {

    private static final Path resourcePath = Paths.get("src/test/resources/configtest");
    public static final AppConfiguration defaultConfig = new AppConfiguration(
        180,
        190,
        840,
        600,
        "azureus-5.7.5.0.client"
    );

    @Test
    public void shouldFailIWithEmptyConfigPath() {
        try {
            final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), " ");
            provider.loadConfiguration();
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualTo("A config path is required.");
        }
    }

    @Test
    public void shouldFailIfJsonFileIsNotPresent() {
        try {
            final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), resourcePath.resolve("dd").toString());
            provider.loadConfiguration();
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualTo("Configuration file 'src\\test\\resources\\configtest\\dd\\config.json' not found.");
        }
    }

    @Test
    public void shouldLoadConf() {
        final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), resourcePath.toString());

        assertThat(provider.get()).isEqualToComparingFieldByField(defaultConfig);
    }

    @Test
    public void shouldLoadConfOnlyOneTimeIfNoDirtyState() {
        final JoalConfigProvider provider = Mockito.spy(new JoalConfigProvider(new ObjectMapper(), resourcePath.toString()));

        provider.get();
        provider.get();

        Mockito.verify(provider, Mockito.times(1)).loadConfiguration();
    }

    @Test
    public void shouldReLoadConfOnlyOneTimeIfSetToDirtyState() {
        final JoalConfigProvider provider = Mockito.spy(new JoalConfigProvider(new ObjectMapper(), resourcePath.toString()));

        provider.get();
        provider.setDirtyState();
        provider.get();

        Mockito.verify(provider, Mockito.times(2)).loadConfiguration();
    }

}
