package org.araymond.joal.web.messages.outgoing.impl.global.state;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 22/06/2017.
 */
public class GlobalSeedStartedPayload implements MessagePayload {

    private final String client;

    public GlobalSeedStartedPayload(final String client) {
        Preconditions.checkArgument(!StringUtils.isBlank(client), "Client must not be null or empty.");

        this.client = client;
    }

    public String getClient() {
        return client;
    }
}
