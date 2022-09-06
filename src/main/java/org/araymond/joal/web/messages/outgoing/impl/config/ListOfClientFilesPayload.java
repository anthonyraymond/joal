package org.araymond.joal.web.messages.outgoing.impl.config;

import lombok.Getter;
import org.araymond.joal.core.events.config.ListOfClientFilesEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.util.List;

/**
 * Created by raymo on 08/07/2017.
 */
@Getter
public class ListOfClientFilesPayload implements MessagePayload {
    private final List<String> clients;

    public ListOfClientFilesPayload(final ListOfClientFilesEvent event) {
        this.clients = event.getClients();
    }
}
