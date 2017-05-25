package org.araymond.joal.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by raymo on 24/01/2017.
 */
public class AppConfiguration {

    private final int minUploadRate;
    private final int maxUploadRate;
    private final int simultaneousSeed;
    private final String client;

    @JsonCreator
    AppConfiguration(
            @JsonProperty(value = "minUploadRate", required = true) final int minUploadRate,
            @JsonProperty(value = "maxUploadRate", required = true) final int maxUploadRate,
            @JsonProperty(value = "simultaneousSeed", required = true) final int simultaneousSeed,
            @JsonProperty(value = "client", required = true) final String client
    ) {
        this.minUploadRate = minUploadRate;
        this.maxUploadRate = maxUploadRate;
        this.simultaneousSeed = simultaneousSeed;
        this.client = client;

        validate();
    }

    public int getMaxUploadRate() {
        return maxUploadRate;
    }

    public int getMinUploadRate() {
        return minUploadRate;
    }

    public int getSimultaneousSeed() {
        return simultaneousSeed;
    }

    @JsonProperty("client")
    public String getClientFileName() {
        return client;
    }

    private void validate() {
        if (minUploadRate < 0) {
            throw new AppConfigurationIntegrityException("minUploadRate must be at least 0.");
        }
        if (maxUploadRate < 0) {
            throw new AppConfigurationIntegrityException("maxUploadRate must greater than 0.");
        }
        if (maxUploadRate <= minUploadRate) {
            throw new AppConfigurationIntegrityException("maxUploadRate must be strictly greater than minUploadRate.");
        }
        if (simultaneousSeed < 1) {
            throw new AppConfigurationIntegrityException("simultaneousSeed must be greater than 0.");
        }
        if (StringUtils.isBlank(client)) {
            throw new AppConfigurationIntegrityException("client is required, no file name given.");
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AppConfiguration that = (AppConfiguration) o;
        return minUploadRate == that.minUploadRate &&
                maxUploadRate == that.maxUploadRate &&
                simultaneousSeed == that.simultaneousSeed &&
                Objects.equal(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(minUploadRate, maxUploadRate, simultaneousSeed, client);
    }
}
