package org.araymond.joal.web.messages.incoming.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.AppConfigurationIntegrityException;

/**
 * Created by raymo on 09/07/2017.
 */
public class ConfigIncomingMessage {
    private final Long minUploadRate;
    private final Long maxUploadRate;
    private final Integer simultaneousSeed;
    private final String client;

    @JsonCreator
    ConfigIncomingMessage(
            @JsonProperty(value = "minUploadRate", required = true) final Long minUploadRate,
            @JsonProperty(value = "maxUploadRate", required = true) final Long maxUploadRate,
            @JsonProperty(value = "simultaneousSeed", required = true) final Integer simultaneousSeed,
            @JsonProperty(value = "client", required = true) final String client
    ) {
        this.minUploadRate = minUploadRate;
        this.maxUploadRate = maxUploadRate;
        this.simultaneousSeed = simultaneousSeed;
        this.client = client;
    }

    public Long getMinUploadRate() {
        return minUploadRate;
    }

    public Long getMaxUploadRate() {
        return maxUploadRate;
    }

    public Integer getSimultaneousSeed() {
        return simultaneousSeed;
    }

    public String getClient() {
        return client;
    }

    public AppConfiguration toAppConfiguration() throws AppConfigurationIntegrityException {
        return new AppConfiguration(this.minUploadRate, this.maxUploadRate, this.simultaneousSeed, this.client);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("minUploadRate", minUploadRate)
                .add("maxUploadRate", maxUploadRate)
                .add("simultaneousSeed", simultaneousSeed)
                .add("client", client)
                .toString();
    }
}
