package org.araymond.joal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import javax.inject.Provider;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 18/04/2017.
 */
public class JoalConfigProviderTest {

    private static final Path resourcePath = Paths.get("src/test/resources/configtest");
    private static final Path rewritableResourcePath = Paths.get("src/test/resources/rewritable-config");
    public static final AppConfiguration defaultConfig = new AppConfiguration(
            180L,
            190L,
            2,
            "azureus-5.7.5.0.client"
    );

    @Test
    public void shouldFailIWithEmptyConfigPath() {
        assertThatThrownBy(() -> new JoalConfigProvider(new ObjectMapper(), " ", Mockito.mock(ApplicationEventPublisher.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A config path is required.");
    }

    @Test
    public void shouldFailIfJsonFileIsNotPresent() {
        final String fakePath = resourcePath.resolve("nop").toString();
        assertThatThrownBy(() -> new JoalConfigProvider(new ObjectMapper(), fakePath, Mockito.mock(ApplicationEventPublisher.class)))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("App configuration file '" + fakePath + "\\config.json' not found.");
    }

    @Test
    public void shouldLoadConf() throws FileNotFoundException {
        final Provider<AppConfiguration> provider = new JoalConfigProvider(new ObjectMapper(), resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));

        assertThat(provider.get()).isEqualToComparingFieldByField(defaultConfig);
    }

    @Test
    public void shouldLoadConfOnlyOneTimeIfNoDirtyState() throws FileNotFoundException {
        final JoalConfigProvider provider = Mockito.spy(new JoalConfigProvider(new ObjectMapper(), resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class)));

        provider.get();
        provider.get();

        Mockito.verify(provider, Mockito.times(1)).loadConfiguration();
    }

    @Test
    public void shouldReLoadConfOnlyOneTimeIfSetToDirtyState() throws FileNotFoundException {
        final JoalConfigProvider provider = Mockito.spy(new JoalConfigProvider(new ObjectMapper(), resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class)));

        provider.get();
        provider.setDirtyState();
        provider.get();

        Mockito.verify(provider, Mockito.times(2)).loadConfiguration();
    }

    @Test
    public void shouldWriteConfigurationFile() throws FileNotFoundException {
        final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), rewritableResourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        final Random rand = new Random();
        final AppConfiguration newConf = new AppConfiguration(
                rand.longs(1, 200).findFirst().getAsLong(),
                rand.longs(201, 400).findFirst().getAsLong(),
                rand.ints(1, 5).findFirst().getAsInt(),
                RandomStringUtils.random(60)
        );

        provider.saveNewConf(newConf);

        assertThat(provider.loadConfiguration()).isEqualTo(newConf);
    }

}
