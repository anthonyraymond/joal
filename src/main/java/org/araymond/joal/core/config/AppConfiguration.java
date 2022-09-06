package org.araymond.joal.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by raymo on 24/01/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Getter
public class AppConfiguration {

    private final Long minUploadRate;
    private final Long maxUploadRate;
    private final Integer simultaneousSeed;
    @JsonProperty("client")
    private final String client;
    @JsonProperty("keepTorrentWithZeroLeechers")
    private final boolean keepTorrentWithZeroLeechers;

    @JsonCreator
    public AppConfiguration(
            @JsonProperty(value = "minUploadRate", required = true) final Long minUploadRate,
            @JsonProperty(value = "maxUploadRate", required = true) final Long maxUploadRate,
            @JsonProperty(value = "simultaneousSeed", required = true) final Integer simultaneousSeed,
            @JsonProperty(value = "client", required = true) final String client,
            @JsonProperty(value = "keepTorrentWithZeroLeechers", required = true) final boolean keepTorrentWithZeroLeechers
    ) {
        this.minUploadRate = minUploadRate;
        this.maxUploadRate = maxUploadRate;
        this.simultaneousSeed = simultaneousSeed;
        this.client = client;
        this.keepTorrentWithZeroLeechers = keepTorrentWithZeroLeechers;

        validate();
    }

    private void validate() {
        if (minUploadRate == null) {
            throw new AppConfigurationIntegrityException("minUploadRate must not be null");
        } else if (minUploadRate < 0L) {
            throw new AppConfigurationIntegrityException("minUploadRate must be at least 0.");
        }

        if (maxUploadRate == null) {
            throw new AppConfigurationIntegrityException("maxUploadRate must not be null");
        } else if (maxUploadRate < 0L) {
            throw new AppConfigurationIntegrityException("maxUploadRate must greater or equal to 0.");
        } else if (maxUploadRate < minUploadRate) {
            throw new AppConfigurationIntegrityException("maxUploadRate must be greater or equal to minUploadRate.");
        }

        if (simultaneousSeed == null) {
            throw new AppConfigurationIntegrityException("simultaneousSeed must not be null");
        } else if (simultaneousSeed < 1) {
            throw new AppConfigurationIntegrityException("simultaneousSeed must be greater than 0.");
        }

        if (StringUtils.isBlank(client)) {
            throw new AppConfigurationIntegrityException("client is required, no file name given.");
        }
    }
}
