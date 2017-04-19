package org.araymond.joal.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by raymo on 24/01/2017.
 */
public class AppConfiguration {

    private int minUploadRate = 180;
    private int maxUploadRate = 195;
    private int seedFor = 840;
    private int waitBetweenSeed = 600;
    @JsonProperty("client")
    private String client = "azureus-5.7.4.0.client";

    AppConfiguration(){

    }

    public AppConfiguration(final int minUploadRate, final int maxUploadRate, final int seedFor, final int waitBetweenSeed, final String client) {
        this.minUploadRate = minUploadRate;
        this.maxUploadRate = maxUploadRate;
        this.seedFor = seedFor;
        this.waitBetweenSeed = waitBetweenSeed;
        this.client = client;
    }

    public int getMaxUploadRate() {
        return maxUploadRate;
    }

    public int getMinUploadRate() {
        return minUploadRate;
    }

    public int getSeedFor() {
        return seedFor;
    }

    public int getWaitBetweenSeed() {
        return waitBetweenSeed;
    }

    @JsonProperty("client")
    public String getClientFileName() {
        return client;
    }

    void validate() {
        if (minUploadRate < 0) {
            throw new ConfigurationIntegrityException("minUploadRate must be at least 0.");
        }
        if (maxUploadRate <= minUploadRate) {
            throw new ConfigurationIntegrityException("maxUploadRate must be greater than minUploadRate.");
        }
        if (seedFor < 1) {
            throw new ConfigurationIntegrityException("seedFor must be greater than 1.");
        }
        if (waitBetweenSeed < 1) {
            throw new ConfigurationIntegrityException("seedFor must be greater than 1.");
        }
        if (StringUtils.isBlank(client)) {
            throw new ConfigurationIntegrityException("client cannot be blank.");
        }
    }

}
