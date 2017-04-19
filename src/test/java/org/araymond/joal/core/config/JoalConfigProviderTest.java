package org.araymond.joal.core.config;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Provider;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by raymo on 18/04/2017.
 */
public class JoalConfigProviderTest {

    static final Path resourcePath = Paths.get("src/test/resources/configtest");
    static final AppConfiguration defaultConfig = new AppConfiguration(
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
        final AppConfiguration conf = provider.loadConfiguration();

        assertThat(conf).isEqualToComparingFieldByField(defaultConfig);
    }

}
