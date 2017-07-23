package org.araymond.joal;

import org.araymond.joal.core.SeedManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by raymo on 08/07/2017.
 */
@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationReadyListener.class);

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
            logger.error("Fatal error encountered", wrapped);
            applicationContext.close();
        }
    }

}
