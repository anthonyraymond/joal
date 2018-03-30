package org.araymond.joal.springtestconf;

import org.araymond.joal.core.SeedManager;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@TestConfiguration
public class MockedSeedManagerBean {

    @Bean
    @Scope("prototype")
    public SeedManager mockedSeedManager() {
        return Mockito.mock(SeedManager.class);
    }

}
