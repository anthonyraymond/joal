package org.araymond.joal.core.bandwith;

import lombok.Getter;
import org.araymond.joal.core.config.AppConfiguration;

import java.util.concurrent.ThreadLocalRandom;

public class RandomSpeedProvider {
    private final AppConfiguration appConf;

    @Getter
    private long currentSpeed;  // bytes/s

    public RandomSpeedProvider(final AppConfiguration appConf) {
        this.appConf = appConf;
        this.refresh();
    }

    public void refresh() {
        final long minUploadRateInBytes = appConf.getMinUploadRate() * 1000L;
        final long maxUploadRateInBytes = appConf.getMaxUploadRate() * 1000L;
        this.currentSpeed = (minUploadRateInBytes == maxUploadRateInBytes)
                ? maxUploadRateInBytes
                : ThreadLocalRandom.current().nextLong(minUploadRateInBytes, maxUploadRateInBytes);
    }
}
