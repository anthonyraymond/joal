package org.araymond.joal.web.services;

import org.araymond.joal.web.messages.outgoing.MessagePayload;
import org.araymond.joal.web.messages.outgoing.StompMessage;
import org.araymond.joal.web.messages.outgoing.impl.announce.AnnouncePayload;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.araymond.joal.web.messages.outgoing.StompMessageTypes.*;

/**
 * Created by raymo on 29/06/2017.
 */
@Service
public class JoalMessageSendingTemplate {

    private final SimpMessageSendingOperations messageSendingOperations;
    private final List<StompMessage> replayablePayloads;
    private final ReentrantReadWriteLock lock;

    public JoalMessageSendingTemplate(final SimpMessageSendingOperations messageSendingOperations) {
        this.messageSendingOperations = messageSendingOperations;
        this.replayablePayloads = new ArrayList<>(30);
        lock = new ReentrantReadWriteLock(true);
    }

    public List<StompMessage> getReplayablePayloads() {
        try {
            lock.readLock().lock();
            return Collections.unmodifiableList(replayablePayloads);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void convertAndSend(final String s, final MessagePayload payload) throws MessagingException {
        final StompMessage stompMessage = StompMessage.wrap(payload);
        addToReplayable(stompMessage);
        messageSendingOperations.convertAndSend(s, stompMessage);
    }


    private void addToReplayable(final StompMessage stompMessage) {
        try {
            lock.writeLock().lock();
            switch (stompMessage.getType()) {
                case SEED_SESSION_HAS_STARTED: {
                    replayablePayloads.removeIf(message -> message.getType() == SEED_SESSION_HAS_STARTED || message.getType() == SEED_SESSION_HAS_ENDED);
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case SEED_SESSION_HAS_ENDED: {
                    replayablePayloads.removeIf(message -> message.getType() == SEED_SESSION_HAS_STARTED || message.getType() == SEED_SESSION_HAS_ENDED);
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case ANNOUNCER_HAS_STARTED: {
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case ANNOUNCER_HAS_STOPPED: {
                    final String id = ((AnnouncePayload) stompMessage.getPayload()).getId();
                    replayablePayloads.removeIf(message -> {
                        //noinspection SimplifiableIfStatement
                        if (!AnnouncePayload.class.isAssignableFrom(message.getPayload().getClass())) {
                            return false;
                        }
                        return ((AnnouncePayload) message.getPayload()).getId().equals(id);
                    });
                    break;
                }
                case ANNOUNCER_WILL_ANNOUNCE: {
                    final String id = ((AnnouncePayload) stompMessage.getPayload()).getId();
                    replayablePayloads.removeIf(message -> {
                        //noinspection SimplifiableIfStatement
                        if (!AnnouncePayload.class.isAssignableFrom(message.getPayload().getClass())) {
                            return false;
                        }
                        return ((AnnouncePayload) message.getPayload()).getId().equals(id)
                                && (message.getType() == ANNOUNCER_HAS_FAILED_TO_ANNOUNCE || message.getType() == ANNOUNCER_HAS_ANNOUNCED || message.getType() == ANNOUNCER_WILL_ANNOUNCE);
                    });
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case ANNOUNCER_HAS_ANNOUNCED: {
                    final String id = ((AnnouncePayload) stompMessage.getPayload()).getId();
                    replayablePayloads.removeIf(message -> {
                        //noinspection SimplifiableIfStatement
                        if (!AnnouncePayload.class.isAssignableFrom(message.getPayload().getClass())) {
                            return false;
                        }
                        return ((AnnouncePayload) message.getPayload()).getId().equals(id)
                                && (message.getType() == ANNOUNCER_HAS_FAILED_TO_ANNOUNCE || message.getType() == ANNOUNCER_HAS_ANNOUNCED || message.getType() == ANNOUNCER_WILL_ANNOUNCE);
                    });
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case ANNOUNCER_HAS_FAILED_TO_ANNOUNCE: {
                    final String id = ((AnnouncePayload) stompMessage.getPayload()).getId();
                    replayablePayloads.removeIf(message -> {
                        //noinspection SimplifiableIfStatement
                        if (!AnnouncePayload.class.isAssignableFrom(message.getPayload().getClass())) {
                            return false;
                        }
                        return ((AnnouncePayload) message.getPayload()).getId().equals(id)
                                && (message.getType() == ANNOUNCER_HAS_FAILED_TO_ANNOUNCE || message.getType() == ANNOUNCER_HAS_ANNOUNCED || message.getType() == ANNOUNCER_WILL_ANNOUNCE);
                    });
                    replayablePayloads.add(stompMessage);
                    break;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

}
