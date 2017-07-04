package org.araymond.joal.web.resources;

import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;

/**
 * Created by raymo on 04/07/2017.
 */
@Controller
@MessageMapping("/summary")
public class SummaryController {

    private final JoalConfigProvider configProvider;
    private final SeedManager seedManager;

    @Inject
    public SummaryController(final JoalConfigProvider configProvider, final SeedManager seedManager) {
        this.configProvider = configProvider;
        this.seedManager = seedManager;
    }



}
