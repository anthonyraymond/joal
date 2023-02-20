package org.araymond.joal.web.messages.incoming.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.AppConfigurationIntegrityException;

/**
 * Created by raymo on 09/07/2017.
 */
@Getter
@ToString
public class ConfigIncomingMessage {
    private final Long minUploadRate;
    private final Long maxUploadRate;
    private final Integer simultaneousSeed;
    private final Float uploadRatioTarget;
    private final String client;
    private final boolean keepTorrentWithZeroLeechers;

    @JsonCreator
    ConfigIncomingMessage(
            @JsonProperty(value = "minUploadRate", required = true) final Long minUploadRate,
            @JsonProperty(value = "maxUploadRate", required = true) final Long maxUploadRate,
            @JsonProperty(value = "simultaneousSeed", required = true) final Integer simultaneousSeed,
            @JsonProperty(value = "client", required = true) final String client,
            @JsonProperty(value = "uploadRatioTarget", defaultValue = "-1.0", required = false) final Float uploadRatioTarget,
            @JsonProperty(value = "keepTorrentWithZeroLeechers", required = true) final boolean keepTorrentWithZeroLeechers
    ) {
        this.minUploadRate = minUploadRate;
        this.maxUploadRate = maxUploadRate;
        this.simultaneousSeed = simultaneousSeed;
        this.client = client;
        this.uploadRatioTarget = uploadRatioTarget;
        this.keepTorrentWithZeroLeechers = keepTorrentWithZeroLeechers;
    }

    public AppConfiguration toAppConfiguration() throws AppConfigurationIntegrityException {
        return new AppConfiguration(this.minUploadRate, this.maxUploadRate, this.simultaneousSeed, this.client, keepTorrentWithZeroLeechers, this.uploadRatioTarget);
    }
}
