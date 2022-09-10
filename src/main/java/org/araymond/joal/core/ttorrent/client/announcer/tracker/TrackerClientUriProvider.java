package org.araymond.joal.core.ttorrent.client.announcer.tracker;

import com.google.common.collect.Iterators;
import lombok.SneakyThrows;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TrackerClientUriProvider {
    private final Iterator<URI> addressIterator;
    private URI currentURI = null;

    @SneakyThrows
    public TrackerClientUriProvider(@SuppressWarnings("TypeMayBeWeakened") final List<URI> trackersURI) {
        List<URI> trackers = trackersURI.stream()
                .filter(uri -> uri.getScheme().startsWith("http"))
                .collect(toList());

        if (trackers.isEmpty()) {
            throw new NoMoreUriAvailableException("No valid http trackers provided");
        }

        // TODO: sorted(new PreferHTTPSComparator())
        this.addressIterator = Iterators.cycle(trackers);
    }

    URI get() {
        if (this.currentURI == null) {
            this.currentURI = this.addressIterator.next();
        }
        return this.currentURI;
    }

    void deleteCurrentAndMoveToNext() throws NoMoreUriAvailableException {
        if (this.currentURI == null) {
            // TODO: shouldn't we throw or ignore if current selection is null?
            this.currentURI = this.addressIterator.next();
        }
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
