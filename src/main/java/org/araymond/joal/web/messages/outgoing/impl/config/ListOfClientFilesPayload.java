package org.araymond.joal.web.messages.outgoing.impl.config;

import org.araymond.joal.core.events.config.ListOfClientFilesEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.util.List;

/**
 * Created by raymo on 08/07/2017.
 */
public class ListOfClientFilesPayload implements MessagePayload {

    private final List<String> clients;

    public ListOfClientFilesPayload(final ListOfClientFilesEvent event) {
        this.clients = event.getClients();
    }

    public List<String> getClients() {
        return clients;
    }
}
