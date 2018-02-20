package org.araymond.joal.core.ttorrent.client.announcer.tracker;

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

public class TrackerClientUriProvider {
    private static final Logger logger = LoggerFactory.getLogger(TrackerClientUriProvider.class);

    private final Iterator<URI> addressIterator;
    private URI currentURI = null;

    public TrackerClientUriProvider(@SuppressWarnings("TypeMayBeWeakened") final List<URI> trackersURI) {
        // TODO: sorted(new PreferHTTPSComparator())
        this.addressIterator = Iterators.cycle(trackersURI);
    }

    URI get() {
        if (this.currentURI == null) {
            this.currentURI = this.addressIterator.next();
        }
        return this.currentURI;
    }

    void deleteCurrentAndMoveToNext() throws NoMoreUriAvailableException {
        this.addressIterator.remove();
        this.moveToNext();
    }

    void moveToNext() throws NoMoreUriAvailableException {
        if (!this.addressIterator.hasNext()) {
            throw new NoMoreUriAvailableException("No more valid trackers");
        }
        this.currentURI = this.addressIterator.next();
    }

}
