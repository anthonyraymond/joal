package org.araymond.joal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import javax.inject.Provider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
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
            5,
            "azureus-5.7.5.0.client",
            false
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
                .hasMessageContaining("App configuration file '" + fakePath + File.separator + "config.json' not found.");
    }

    @Test
    public void shouldFailToGetConfIfNotInitialized() throws FileNotFoundException {
        final Provider<AppConfiguration> provider = new JoalConfigProvider(new ObjectMapper(), resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));

        assertThatThrownBy(provider::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Attempted to get configuration before init.");
    }

    @Test
    public void shouldLoadCong() throws FileNotFoundException {
        final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));

        assertThat(provider.loadConfiguration()).isEqualToComparingFieldByField(defaultConfig);
    }

    @Test
    public void shouldGetConf() throws FileNotFoundException {
        final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        provider.init();

        assertThat(provider.get()).isEqualTo(defaultConfig);
    }

    @Test
    public void shouldAlwaysReturnsSameConf() throws FileNotFoundException {
        final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), resourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
        provider.init();

        assertThat(provider.get()).usingComparator((Comparator<AppConfiguration>) (o1, o2) -> {
            if (o1 == o2) return 0;
            return -1;
        })
                .isEqualTo(provider.get())
                .isEqualTo(provider.get());
    }

    @Test
    public void shouldPublishConfigHasBeenLoadedEventOnConfigLoad() throws FileNotFoundException {
        final ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), resourcePath.toString(), publisher);

        final AppConfiguration loadedConf = provider.loadConfiguration();

        final ArgumentCaptor<ConfigHasBeenLoadedEvent> captor = ArgumentCaptor.forClass(ConfigHasBeenLoadedEvent.class);
        Mockito.verify(publisher, Mockito.times(1)).publishEvent(captor.capture());

        final ConfigHasBeenLoadedEvent event = captor.getValue();
        assertThat(event.getConfiguration()).isEqualTo(loadedConf);
    }

    @Test
    public void shouldWriteConfigurationFile() throws IOException {
        new ObjectMapper().writeValue(rewritableResourcePath.resolve("config.json").toFile(), defaultConfig);
        try {
            final JoalConfigProvider provider = new JoalConfigProvider(new ObjectMapper(), rewritableResourcePath.toString(), Mockito.mock(ApplicationEventPublisher.class));
            final Random rand = new Random();
            final AppConfiguration newConf = new AppConfiguration(
                    rand.longs(1, 200).findFirst().getAsLong(),
                    rand.longs(201, 400).findFirst().getAsLong(),
                    rand.ints(1, 5).findFirst().getAsInt(),
                    RandomStringUtils.random(60),
                    false
            );

            provider.saveNewConf(newConf);

            assertThat(provider.loadConfiguration()).isEqualTo(newConf);
        } finally {
            Files.deleteIfExists(rewritableResourcePath.resolve("config.json"));
        }
    }

}
