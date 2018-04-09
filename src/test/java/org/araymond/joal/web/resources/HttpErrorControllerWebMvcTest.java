package org.araymond.joal.web.resources;

import org.araymond.joal.web.config.EndpointObfuscatorConfiguration;
import org.araymond.joal.web.config.JacksonConfig;
import org.araymond.joal.web.config.WebMvcConfiguration;
import org.araymond.joal.web.config.WebSocketConfig;
import org.araymond.joal.web.config.security.WebSecurityConfig;
import org.araymond.joal.web.config.security.WebSocketAuthenticationSecurityConfig;
import org.araymond.joal.web.config.security.WebSocketAuthorizationSecurityConfig;
import org.araymond.joal.web.config.security.websocket.interceptor.AuthChannelInterceptorAdapter;
import org.araymond.joal.web.config.security.websocket.services.WebSocketAuthenticatorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@WebMvcTest(
        controllers = { HttpErrorController.class },
        secure = false, excludeAutoConfiguration = { BasicErrorController.class, ErrorMvcAutoConfiguration.class }
)
@TestPropertySource(properties = "spring.main.web-environment=true")
public class HttpErrorControllerWebMvcTest {

    @Inject
    private MockMvc mvc;

    @Test
    public void should() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/error"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

}

