package org.araymond.joal.web.resources;

import org.araymond.joal.web.messages.outgoing.StompMessage;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by raymo on 27/07/2017.
 */
@ConditionalOnProperty(name = "spring.main.web-environment", havingValue = "true")
@Controller
public class RePlayableEventsController {

    private final JoalMessageSendingTemplate messageSendingTemplate;

    @Inject
    public RePlayableEventsController(final JoalMessageSendingTemplate messageSendingTemplate) {
        this.messageSendingTemplate = messageSendingTemplate;
    }

    /**
     * This mapping is bypassing the spring WebSocket broker (because of SubscribeMapping) and send the response
     * directly to the client who subscribed, and only him.
     *
     * @return an ordered list of all needed event to rebuild the current application state
     */
    @SubscribeMapping("/events/replay")
    public List<StompMessage> list() {
        return messageSendingTemplate.getReplayablePayloads();
    }

}
