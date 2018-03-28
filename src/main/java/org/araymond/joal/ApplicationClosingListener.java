package org.araymond.joal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.araymond.joal.core.SeedManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by raymo on 08/07/2017.
 */
@Component
public class ApplicationClosingListener implements ApplicationListener<ContextClosedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationClosingListener.class);

    private final SeedManager manager;

    @Inject
    public ApplicationClosingListener(final SeedManager manager) {
        this.manager = manager;
    }

    @Override
    public void onApplicationEvent(final ContextClosedEvent event) {
        logger.info("Gracefully shutting down application.");
        manager.stop();
        manager.tearDown();
        logger.info("JOAL gracefully shut down.");

        // Since we disabled log4j2 shutdown hook, we need to handle it manually.
        final LifeCycle loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.stop();
    }

}
