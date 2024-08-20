package org.araymond.joalcore.core.trackers.domain;

import java.time.Duration;
import java.util.Objects;

public record AnnounceSucceed(Duration interval) {
    public AnnounceSucceed {
        Objects.requireNonNull("Duration required a non-null interval");
    }
}
