package org.araymond.joal.core.config;

import org.araymond.joal.core.utils.MockedInjections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 19/04/2017.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JoalConfigProvider.class, MockedInjections.DefaultObjectMapperDI.class})
@TestPropertySource(properties = {
        "joal-conf=src/test/resources/configtest",
})
public class JoalConfigProviderDITest {

    @Inject
    private JoalConfigProvider configProvider;

    @Test
    public void shouldInjectConfigProvider() {
        assertThat(configProvider.get()).isEqualToComparingFieldByField(JoalConfigProviderTest.defaultConfig);
    }

}
