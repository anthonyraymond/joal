package org.araymond.joal.web.config;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConditionalOnWebUi
// Do not use @EnableWebMvc as it will remove all the default springboot config.
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final String[] RESOURCE_LOCATIONS = new String[]{"classpath:/public/"};

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/ui/**")) {
            registry.addResourceHandler("/ui/**")
                    .addResourceLocations(RESOURCE_LOCATIONS);
        }
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        // The webui passes the credentials alongs with ui call, redirect them as well
        registry.addRedirectViewController("/ui", "ui/").setKeepQueryParams(true);
        registry.addViewController("/ui/").setViewName("forward:index.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        //super.addViewControllers(registry);
    }
}
