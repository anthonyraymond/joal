package org.araymond.joal.web.resources;

import org.araymond.joal.DefaultWebContextTest;
import org.junit.Test;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpSecurityWebContextTest extends DefaultWebContextTest {

    @LocalServerPort
    private int port;

    @Inject
    private TestRestTemplate restTemplate;

    @Test
    public void shouldObfuscatePathAndAnswer404WhenCallUiWithoutSecretPath() throws Exception {
        final ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/ui/", String.class);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    public void shouldObfuscatePathAndAnswerWhenCallUiWithSecretPath() throws Exception {
        final ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/" + DefaultWebContextTest.UI_PATH_PREFIX + "/ui/", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    public void shouldPreventAccessingAnyOtherPaths() {
        final ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/whatever/", String.class);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    public void shouldReturn404OnNonPrefixedPath() {
        final ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/whatever/", String.class);
        // TODO : decide whether we should return 404 or 403
        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void shouldReturn404OnInvalidPrefixedPath() {
        final ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/" + DefaultWebContextTest.UI_PATH_PREFIX + "/whatever/", String.class);
        // TODO : decide whether we should return 404 or 403
        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void shouldPermitHttpCallOnPrefixedPathForWebSocketHandshake() {
        final ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/" + DefaultWebContextTest.UI_PATH_PREFIX, String.class);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).containsIgnoringCase("upgrade").containsIgnoringCase("websocket");
    }

}
