package org.araymond.joal.core.events.config;

import org.araymond.joal.core.config.AppConfiguration;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 09/07/2017.
 */
public class ConfigHasChangedEventTest {

    @Test
    public void shouldNotBuildWithoutConfig() {
        assertThatThrownBy(() -> new ConfigHasChangedEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Configuration must not be null.");
    }

    @Test
    public void shouldBuild() {
        final AppConfiguration config = Mockito.mock(AppConfiguration.class);
        final ConfigHasChangedEvent event = new ConfigHasChangedEvent(config);

        assertThat(event.getConfiguration()).isEqualTo(config);
    }

}
