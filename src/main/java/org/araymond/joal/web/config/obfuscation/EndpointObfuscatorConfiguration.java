package org.araymond.joal.web.config.obfuscation;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Created by raymo on 25/07/2017.
 */
@ConditionalOnWebUi
@Configuration
public class EndpointObfuscatorConfiguration {

    private final String pathPrefix;

    public EndpointObfuscatorConfiguration(@Value("${joal.ui.path.prefix}") final String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    @Bean
    public ServletRegistrationBean obfuscatedDispatcherServlet() {
        final DispatcherServlet dispatcherServlet = new DispatcherServlet();

        final ApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        dispatcherServlet.setApplicationContext(applicationContext);

        final ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet, "/" + this.pathPrefix + "/*");
        servletRegistrationBean.setName("joal");
        return servletRegistrationBean;
    }
}
