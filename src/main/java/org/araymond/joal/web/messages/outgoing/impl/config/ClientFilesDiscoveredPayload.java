package org.araymond.joal.web.messages.outgoing.impl.config;

import org.araymond.joal.core.events.old.config.ClientFilesDiscoveredEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.util.List;

/**
 * Created by raymo on 08/07/2017.
 */
public class ClientFilesDiscoveredPayload implements MessagePayload {

    private final List<String> clients;

    public ClientFilesDiscoveredPayload(final ClientFilesDiscoveredEvent event) {
        this.clients = event.getClients();
    }

    public List<String> getClients() {
        return clients;
    }
}
