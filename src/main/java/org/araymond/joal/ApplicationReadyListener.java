package org.araymond.joal;

import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.SeedManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by raymo on 08/07/2017.
 */
@Profile("!test")
@Component
@Slf4j
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    private final SeedManager manager;
    private final ConfigurableApplicationContext applicationContext;

    @Inject
    public ApplicationReadyListener(final SeedManager manager, final ConfigurableApplicationContext applicationContext) {
        this.manager = manager;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            manager.init();
            manager.startSeeding();
        } catch (final Throwable e) {
            final IllegalStateException wrapped = new IllegalStateException("Fatal error encountered", e);
            log.error("Fatal error encountered", wrapped);
            applicationContext.close();
        }
    }

}
