package org.araymond.joal.core.client.emulated.generator.numwant;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.EqualsAndHashCode;

/**
 * Created by raymo on 19/07/2017.
 */
@EqualsAndHashCode
public class NumwantProvider {

    private final int numwant;
    private final int numwantOnStop;

    public NumwantProvider(final int numwant, final int numwantOnStop) {
        Preconditions.checkArgument(numwant > 0, "numwant must be at least 1");
        Preconditions.checkArgument(numwantOnStop >= 0, "numwantOnStop must be at least 0");
        this.numwant = numwant;
        this.numwantOnStop = numwantOnStop;
    }

    public int get(final RequestEvent event) {
        return event == RequestEvent.STOPPED ? numwantOnStop : numwant;
    }
}
