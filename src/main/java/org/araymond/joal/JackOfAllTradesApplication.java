package org.araymond.joal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class JackOfAllTradesApplication {
    private static final Logger logger = LoggerFactory.getLogger(JackOfAllTradesApplication.class);

    public static void main(final String[] args) {
        SpringApplication.run(JackOfAllTradesApplication.class, args);
    }


}