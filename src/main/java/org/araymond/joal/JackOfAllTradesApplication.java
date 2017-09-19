package org.araymond.joal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class JackOfAllTradesApplication {

    public static void main(final String[] args) {
        SpringApplication.run(JackOfAllTradesApplication.class, args);
    }

}