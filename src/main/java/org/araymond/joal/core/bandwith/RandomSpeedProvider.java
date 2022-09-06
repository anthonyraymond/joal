package org.araymond.joal.core.bandwith;

import lombok.Getter;
import org.araymond.joal.core.config.AppConfiguration;

import java.util.concurrent.ThreadLocalRandom;

public class RandomSpeedProvider {
    private final AppConfiguration appConfiguration;

    @Getter
    private long currentSpeed;  // bytes/s

    public RandomSpeedProvider(final AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
        this.refresh();
    }

    public void refresh() {
        final long minUploadRateInBytes = appConfiguration.getMinUploadRate() * 1000L;
        final long maxUploadRateInBytes = appConfiguration.getMaxUploadRate() * 1000L;
        this.currentSpeed = (minUploadRateInBytes == maxUploadRateInBytes)
                ? maxUploadRateInBytes
                : ThreadLocalRandom.current().nextLong(minUploadRateInBytes, maxUploadRateInBytes);
    }
}
