package org.araymond.joal.web.services;

import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.outgoing.MessagePayload;
import org.araymond.joal.web.messages.outgoing.StompMessage;
import org.araymond.joal.web.messages.outgoing.impl.announce.AnnouncePayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileAddedPayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileDeletedPayload;
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
@ConditionalOnWebUi
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
                case GLOBAL_SEED_STARTED: {
                    replayablePayloads.removeIf(message -> message.getType() == GLOBAL_SEED_STARTED || message.getType() == GLOBAL_SEED_STOPPED);
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case GLOBAL_SEED_STOPPED: {
                    replayablePayloads.removeIf(message ->
                            message.getType() != CONFIG_HAS_BEEN_LOADED
                                    && message.getType() != CONFIG_IS_IN_DIRTY_STATE
                                    && message.getType() != LIST_OF_CLIENT_FILES
                    );
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case CONFIG_HAS_BEEN_LOADED: {
                    replayablePayloads.removeIf(message -> message.getType() == CONFIG_HAS_BEEN_LOADED && message.getType() == CONFIG_IS_IN_DIRTY_STATE);
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case CONFIG_IS_IN_DIRTY_STATE: {
                    replayablePayloads.removeIf(message -> message.getType() == CONFIG_IS_IN_DIRTY_STATE);
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case INVALID_CONFIG: {
                    break;
                }
                case LIST_OF_CLIENT_FILES: {
                    replayablePayloads.removeIf(message -> message.getType() == LIST_OF_CLIENT_FILES);
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case TORRENT_FILE_ADDED: {
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case TORRENT_FILE_DELETED: {
                    replayablePayloads.removeIf(message -> {
                        if (message.getType() != TORRENT_FILE_ADDED) {
                            return false;
                        }
                        final TorrentFileDeletedPayload newMsg = (TorrentFileDeletedPayload) stompMessage.getPayload();
                        return ((TorrentFileAddedPayload) message.getPayload()).getInfoHash().equals(newMsg.getInfoHash());
                    });
                    break;
                }
                case FAILED_TO_ADD_TORRENT_FILE: {
                    break;
                }
                case WILL_ANNOUNCE: {
                    final InfoHash infoHash = ((AnnouncePayload) stompMessage.getPayload()).getInfoHash();
                    replayablePayloads.removeIf(message -> {
                        //noinspection SimplifiableIfStatement
                        if (!AnnouncePayload.class.isAssignableFrom(message.getPayload().getClass())) {
                            return false;
                        }

                        return ((AnnouncePayload) message.getPayload()).getInfoHash().equals(infoHash);
                    });
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case SUCCESSFULLY_ANNOUNCE: {
                    final InfoHash infoHash = ((AnnouncePayload) stompMessage.getPayload()).getInfoHash();
                    replayablePayloads.removeIf(message -> {
                        //noinspection SimplifiableIfStatement
                        if (!AnnouncePayload.class.isAssignableFrom(message.getPayload().getClass())) {
                            return false;
                        }

                        return ((AnnouncePayload) message.getPayload()).getInfoHash().equals(infoHash);
                    });
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case FAILED_TO_ANNOUNCE: {
                    final InfoHash infoHash = ((AnnouncePayload) stompMessage.getPayload()).getInfoHash();
                    replayablePayloads.removeIf(message -> {
                        //noinspection SimplifiableIfStatement
                        if (!AnnouncePayload.class.isAssignableFrom(message.getPayload().getClass())) {
                            return false;
                        }

                        return ((AnnouncePayload) message.getPayload()).getInfoHash().equals(infoHash);
                    });
                    replayablePayloads.add(stompMessage);
                    break;
                }
                case TOO_MANY_ANNOUNCES_FAILED: {
                    final InfoHash infoHash = ((AnnouncePayload) stompMessage.getPayload()).getInfoHash();
                    replayablePayloads.removeIf(message -> {
                        //noinspection SimplifiableIfStatement
                        if (!AnnouncePayload.class.isAssignableFrom(message.getPayload().getClass())) {
                            return false;
                        }

                        return ((AnnouncePayload) message.getPayload()).getInfoHash().equals(infoHash);
                    });
                    break;
                }
                case SEEDING_SPEED_HAS_CHANGED: {
                    this.replayablePayloads.removeIf(message -> message.getType() == SEEDING_SPEED_HAS_CHANGED);
                    this.replayablePayloads.add(stompMessage);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

}
