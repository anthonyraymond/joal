package org.araymond.joal.web.messages.outgoing.impl.config;

import org.araymond.joal.core.events.config.ListOfClientFilesEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.util.List;

/**
 * Created by raymo on 08/07/2017.
 */
public class ClientFilesDiscoveredPayload implements MessagePayload {

    private final List<String> clients;

    public ClientFilesDiscoveredPayload(final ListOfClientFilesEvent event) {
        this.clients = event.getClients();
    }

    public List<String> getClients() {
        return clients;
    }
}
