package org.araymond.joal.web.config;

import org.araymond.joal.TestConstant;
import org.araymond.joal.web.config.security.WebSecurityConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                EndpointObfuscatorConfiguration.class,
                org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration.class,
                org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.HttpEncodingAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.class,
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-environment=true",
                "joal.ui.path.prefix=" + TestConstant.UI_PATH_PREFIX,
                "joal.ui.secret-token=" + TestConstant.UI_SECRET_TOKEN
        }
)
@Import({EndpointObfuscatorConfigurationTest.TestController.class})
public class EndpointObfuscatorConfigurationTest {

    @LocalServerPort
    private int port;

    @Inject
    private TestRestTemplate restTemplate;

    @RestController
    public static class TestController {
        @RequestMapping(path = "/hello", method = RequestMethod.GET)
        public String hello() {
            return "hello again";
        }
    }

    @Test
    public void shouldObfuscateSpringsUri() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity(
                "http://localhost:" + port + "/" + TestConstant.UI_PATH_PREFIX + "/hello",
                String.class
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("hello again");
    }

    @Test
    public void shouldNotRespondToNonObfuscateUri() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity(
                "http://localhost:" + port + "/hello",
                String.class
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

}
