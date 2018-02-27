package org.araymond.joal.core.ttorrent.client;

import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.AppConfigurationTest;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnouncerExecutor;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {

    private AppConfiguration createMockedConf() {
        // wrap into spy to allow mocking
        return Mockito.spy(AppConfigurationTest.createOne());
    }

    private AnnounceDataAccessor createMockedAnnouncerFactory(final InfoHash infoHash) {
        return null;
    }



}
