package org.araymond.joal.core.events.config;

import com.google.common.base.Preconditions;

import java.util.List;

public class ListOfClientFilesEvent {
    private final List<String> clients;

    public ListOfClientFilesEvent(final List<String> clients) {
        Preconditions.checkNotNull(clients, "Clients list must not be null");
        this.clients = clients;
    }

    public List<String> getClients() {
        return clients;
    }
}
