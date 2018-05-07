package org.araymond.joal.web.config;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Created by raymo on 25/07/2017.
 */
@ConditionalOnWebUi
@Configuration
public class EndpointObfuscatorConfiguration {

    private final String pathPrefix;

    public EndpointObfuscatorConfiguration(@Value("${joal.ui.path.prefix}")final String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public ServletRegistrationBean dispatcherServletRegistration() {
        final ServletRegistrationBean registration = new ServletRegistrationBean(
                dispatcherServlet(),
                "/" + this.pathPrefix + "/*"
        );
        registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
        return registration;
    }
}
