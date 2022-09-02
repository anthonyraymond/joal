package org.araymond.joal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
// Disable the default Dispatcher servlet registered at "/"
@SpringBootApplication(exclude = { DispatcherServletAutoConfiguration.class, ErrorMvcAutoConfiguration.class })
public class JackOfAllTradesApplication {

    public static void main(final String[] args) {
        SpringApplication.run(JackOfAllTradesApplication.class, args);
    }
}
