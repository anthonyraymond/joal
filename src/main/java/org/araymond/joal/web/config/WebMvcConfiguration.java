package org.araymond.joal.web.config;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.*;

@ConditionalOnWebUi
// Do not use @EnableWebMvc as it will remove all the default springboot config.
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
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
        registry.addViewController("/ui").setViewName("redirect:ui/");
        registry.addViewController("/ui/").setViewName("forward:index.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        super.addViewControllers(registry);
    }

}
