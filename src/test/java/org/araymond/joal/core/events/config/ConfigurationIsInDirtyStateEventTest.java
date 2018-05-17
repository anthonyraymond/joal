package org.araymond.joal.core.events.config;

import org.araymond.joal.core.config.AppConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ConfigurationIsInDirtyStateEventTest {

    @Test
    public void shouldBuild() {
        final AppConfiguration appConfiguration = mock(AppConfiguration.class);
        final ConfigurationIsInDirtyStateEvent event = new ConfigurationIsInDirtyStateEvent(appConfiguration);

        assertThat(event.getConfiguration()).isEqualTo(appConfiguration);
    }

}
