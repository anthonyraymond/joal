package org.araymond.joal.web.resources;

import org.araymond.joal.web.messages.outgoing.StompMessage;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by raymo on 04/07/2017.
 */
@RestController
@RequestMapping("/replayable")
public class SummaryController {

    private final JoalMessageSendingTemplate messageSendingTemplate;

    @Inject
    public SummaryController(final JoalMessageSendingTemplate messageSendingTemplate) {
        this.messageSendingTemplate = messageSendingTemplate;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<StompMessage> replayableState() {
        return this.messageSendingTemplate.getReplayablePayloads();
    }


}
