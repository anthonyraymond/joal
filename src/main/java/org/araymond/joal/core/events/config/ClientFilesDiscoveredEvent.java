package org.araymond.joal.core.events.config;

import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by raymo on 08/07/2017.
 */
public class ClientFilesDiscoveredEvent {
    private final List<String> clients;

    public ClientFilesDiscoveredEvent(final List<String> clients) {
        Preconditions.checkNotNull(clients, "Clients list must not be null");
        this.clients = clients;
    }

    public List<String> getClients() {
        return clients;
    }

}

