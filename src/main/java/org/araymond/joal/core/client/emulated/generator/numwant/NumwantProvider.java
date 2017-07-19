package org.araymond.joal.core.client.emulated.generator.numwant;

import com.google.common.base.Preconditions;

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

    public Integer getNumwant() {
        return numwant;
    }

    public Integer getNumwantOnStop() {
        return numwantOnStop;
    }
}
