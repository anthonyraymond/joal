package org.araymond.joal.web.resources;

import org.araymond.joal.core.SeedManager;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by raymo on 24/06/2017.
 */
@Controller
@MessageMapping("/global")
public class GlobalController {

    private final SeedManager seedManager;

    @Inject
    public GlobalController(final SeedManager seedManager) {
        this.seedManager = seedManager;
    }


    @MessageMapping("/start")
    public void stopStartSession() throws IOException {
        seedManager.startSeeding();
    }

    @MessageMapping("/stop")
    public void stopSeedSession() {
        seedManager.stop();
    }

}
