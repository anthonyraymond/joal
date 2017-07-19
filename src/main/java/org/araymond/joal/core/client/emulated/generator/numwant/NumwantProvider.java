package org.araymond.joal.core.client.emulated.generator.numwant;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;

/**
 * Created by raymo on 19/07/2017.
 */
public class NumwantProvider {

    private final Integer numwant;
    private final Integer numwantOnStop;

    public NumwantProvider(final Integer numwant, final Integer numwantOnStop) {
        Preconditions.checkNotNull(numwant, "numwant must not be null.");
        Preconditions.checkArgument(numwant > 0, "numwant must be at least 1.");
        Preconditions.checkNotNull(numwantOnStop, "numwantOnStop must not be null.");
        Preconditions.checkArgument(numwantOnStop >= 0, "numwantOnStop must be at least 0.");
        this.numwant = numwant;
        this.numwantOnStop = numwantOnStop;
    }

    public Integer get(final RequestEvent event) {
        if (event == RequestEvent.STOPPED) {
            return numwantOnStop;
        }
        return numwant;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final NumwantProvider that = (NumwantProvider) o;
        return Objects.equal(numwant, that.numwant) &&
                Objects.equal(numwantOnStop, that.numwantOnStop);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(numwant, numwantOnStop);
    }
}
