package org.araymond.joal.core.ttorrent.client;

import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;

import java.util.List;

public interface ClientFacade {
    void start();
    void stop();
    List<AnnouncerFacade> getCurrentlySeedingAnnouncers();
}
