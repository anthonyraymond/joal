package org.araymond.joal.web.config.obfuscation;

import org.apache.http.NoHttpResponseException;
import org.araymond.joal.TestConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                AbortNonPrefixedRequestFilter.class,
                org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration.class,
                org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration.class,
                org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-environment=true",
                "joal.ui.path.prefix=" + TestConstant.UI_PATH_PREFIX
        }
)
@Import({ AbortNonPrefixedRequestFilterTest.UnprefixedController.class, AbortNonPrefixedRequestFilterTest.PrefixedController.class })
public class AbortNonPrefixedRequestFilterTest {
    @LocalServerPort
    private int port;

    @Inject
    private TestRestTemplate restTemplate;

    @RestController
    public static class UnprefixedController {
        @RequestMapping(path = "/hello", method = RequestMethod.GET)
        public String hello() {
            return "this should not been reached :)";
        }
    }

    @RestController
    public static class PrefixedController {
        @RequestMapping(path = "/" + TestConstant.UI_PATH_PREFIX + "/hello", method = RequestMethod.GET)
        public String hello() {
            return "hello prefixed";
        }
    }

    @Test
    public void shouldHaveNoResponseFromUnprefixedRequest() {
        try {
            final ResponseEntity<String> response = this.restTemplate.getForEntity(
                    "http://localhost:" + port + "/hello",
                    String.class
            );
            fail("shouldn't have had a response");
        } catch (final ResourceAccessException e) {
            assertThat(e).hasCauseInstanceOf(NoHttpResponseException.class);
        }
    }

    @Test
    public void shouldHaveResponseFromPrefixedRequest() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity(
                "http://localhost:" + port + "/" + TestConstant.UI_PATH_PREFIX + "/hello",
                String.class
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("hello prefixed");
    }
}
