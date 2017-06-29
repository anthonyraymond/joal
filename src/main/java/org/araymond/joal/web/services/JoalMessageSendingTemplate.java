package org.araymond.joal.web.services;

import org.araymond.joal.web.messages.outgoing.MessagePayload;
import org.araymond.joal.web.messages.outgoing.StompMessage;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by raymo on 29/06/2017.
 */
@Service
public class JoalMessageSendingTemplate {

    private final SimpMessageSendingOperations messageSendingOperations;

    public JoalMessageSendingTemplate(final SimpMessageSendingOperations messageSendingOperations) {
        this.messageSendingOperations = messageSendingOperations;
    }

    public void convertAndSendToUser(final String s, final String s1, final MessagePayload payload) throws MessagingException {
        messageSendingOperations.convertAndSendToUser(s, s1, StompMessage.wrap(payload));
    }

    public void convertAndSendToUser(final String s, final String s1, final MessagePayload payload, final Map<String, Object> map) throws MessagingException {
        messageSendingOperations.convertAndSendToUser(s, s1, StompMessage.wrap(payload), map);
    }

    public void convertAndSendToUser(final String s, final String s1, final MessagePayload payload, final MessagePostProcessor messagePostProcessor) throws MessagingException {
        messageSendingOperations.convertAndSendToUser(s, s1, StompMessage.wrap(payload), messagePostProcessor);
    }

    public void convertAndSendToUser(final String s, final String s1, final MessagePayload payload, final Map<String, Object> map, final MessagePostProcessor messagePostProcessor) throws MessagingException {
        messageSendingOperations.convertAndSendToUser(s, s1, StompMessage.wrap(payload), map, messagePostProcessor);
    }

    public void convertAndSend(final MessagePayload payload) throws MessagingException {
        messageSendingOperations.convertAndSend(StompMessage.wrap(payload));
    }

    public void convertAndSend(final String s, final MessagePayload payload) throws MessagingException {
        messageSendingOperations.convertAndSend(s, StompMessage.wrap(payload));
    }

    public void convertAndSend(final String s, final MessagePayload payload, final Map<String, Object> map) throws MessagingException {
        messageSendingOperations.convertAndSend(s, StompMessage.wrap(payload), map);
    }

    public void convertAndSend(final MessagePayload payload, final MessagePostProcessor messagePostProcessor) throws MessagingException {
        messageSendingOperations.convertAndSend(payload, messagePostProcessor);
    }

    public void convertAndSend(final String s, final MessagePayload payload, final MessagePostProcessor messagePostProcessor) throws MessagingException {
        messageSendingOperations.convertAndSend(s, StompMessage.wrap(payload), messagePostProcessor);
    }

    public void convertAndSend(final String s, final MessagePayload payload, final Map<String, Object> map, final MessagePostProcessor messagePostProcessor) throws MessagingException {
        messageSendingOperations.convertAndSend(s, StompMessage.wrap(payload), map, messagePostProcessor);
    }
}
