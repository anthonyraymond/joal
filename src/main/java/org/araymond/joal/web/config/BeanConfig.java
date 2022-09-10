package org.araymond.joal.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.SeedManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Created by raymo on 24/07/2017.
 */
@Configuration
public class BeanConfig {

    @Bean
    public SeedManager seedManager(@Value("${joal-conf}") final String joalConfFolder,
                                   final ObjectMapper mapper, final ApplicationEventPublisher publisher) throws IOException {
        return new SeedManager(joalConfFolder, mapper, publisher);
    }
}
