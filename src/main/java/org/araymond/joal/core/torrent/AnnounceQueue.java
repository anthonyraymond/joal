package org.araymond.joal.core.torrent;

import com.google.common.collect.Lists;
import org.araymond.joal.core.torrent.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.utils.AvailableAfterIntervalQueue;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class AnnounceQueue {
    
    private final Queue<Announcer> announcersToStart;
    private final AvailableAfterIntervalQueue<Announcer> announceAfterIntervalQueue;
    private final Queue<Announcer> announcersToStop;
    private final ReentrantLock lock;

    public AnnounceQueue() {
        announcersToStart = new ConcurrentLinkedQueue<>();
        announceAfterIntervalQueue = new AvailableAfterIntervalQueue<>();
        announcersToStop = new ConcurrentLinkedQueue<>();
        lock = new ReentrantLock();
    }

    public void addToStart(final Announcer announcer) {
        lock.lock();
        try {
            this.announcersToStart.add(announcer);
        } finally {
            lock.unlock();
        }
    }

    public void addToInterval(final Announcer announcer, final int interval) {
        lock.lock();
        try {
            this.announceAfterIntervalQueue.add(announcer, interval, ChronoUnit.SECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void addToStop(final Announcer announcer) {
        lock.lock();
        try {
            this.announcersToStop.add(announcer);
        } finally {
            lock.unlock();
        }
    }

    public void moveAllToStop() {
        lock.lock();
        try {
            this.announcersToStart.clear();
            this.announcersToStop.addAll(this.announceAfterIntervalQueue.drainAll());
        } finally {
            lock.unlock();
        }
    }

    public List<Announcer> getAnnouncersToStart() {
        lock.lock();
        try {
            final List<Announcer> availableAnnouncers = Lists.newArrayList();
            availableAnnouncers.addAll(this.announcersToStart);
            this.announcersToStart.clear();
            return availableAnnouncers;
        } finally {
            lock.unlock();
        }
    }

    public List<Announcer> getRegularAnnouncers() {
        lock.lock();
        try {
            return this.announceAfterIntervalQueue.getAvailable();
        } finally {
            lock.unlock();
        }
    }

    public List<Announcer> getAnnouncersToStop() {
        lock.lock();
        try {
            final List<Announcer> availableAnnouncers = Lists.newArrayList();
            availableAnnouncers.addAll(this.announcersToStop);
            this.announcersToStop.clear();
            return availableAnnouncers;
        } finally {
            lock.unlock();
        }
    }

}
