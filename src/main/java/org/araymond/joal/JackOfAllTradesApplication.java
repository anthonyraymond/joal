package org.araymond.joal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class JackOfAllTradesApplication {
    private static final Logger logger = LoggerFactory.getLogger(JackOfAllTradesApplication.class);

    public static void main(final String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(JackOfAllTradesApplication.class, args);
        /*final SeedManager manager = context.getBean(SeedManager.class);

        //noinspection Convert2Lambda
        context.addApplicationListener(new ApplicationListener<ContextClosedEvent>() {
            @Override
            public void onApplicationEvent(final ContextClosedEvent event) {
                logger.info("Gracefully shutting down application.");
                manager.stop();
                logger.info("Application gracefully shut down.");

                // Since we disabled log4j2 shutdown hook, we need to handle it manually.
                final LifeCycle loggerContext = (LoggerContext) LogManager.getContext(false);
                loggerContext.stop();
            }
        });
        try {
            manager.startSeeding();
        } catch (final Throwable e) {
            final IllegalStateException wrapped = new IllegalStateException("Fatal error encountered", e);
            logger.error("Fatal error encountered", wrapped);
            context.close();
        }*/
    }


}