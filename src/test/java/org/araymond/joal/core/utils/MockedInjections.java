package org.araymond.joal.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by raymo on 19/04/2017.
 */
public class MockedInjections {

    @Configuration
    public static class DefaultObjectMapperDI {

        @Bean
        public ObjectMapper mapper() {
            return new ObjectMapper();
        }

    }
}
