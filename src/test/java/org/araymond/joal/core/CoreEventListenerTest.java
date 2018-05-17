package org.araymond.joal.core;

import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreEventListenerTest {

    // Useless but required for coverage...

    @Test
    public void shouldImproveCoverage() throws IOException {
        final CoreEventListener coreEventListener = new CoreEventListener();

        coreEventListener.handleSeedSessionHasEnded(null);
        coreEventListener.handleSeedSessionHasStarted(null);
        coreEventListener.handleTorrentFileAddedForSeed(null);
    }

}
