package org.araymond.joal.core.bandwith;

import org.araymond.joal.core.config.JoalConfigProvider;

import java.util.concurrent.ThreadLocalRandom;

public class RandomSpeedProvider {
    private final JoalConfigProvider configProvider;
    private long currentSpeed;

    public RandomSpeedProvider(final JoalConfigProvider configProvider) {
        this.configProvider = configProvider;

        this.refresh();
    }

    public void refresh() {
        final Long minUploadRateInBytes = configProvider.get().getMinUploadRate() * 1000L;
        final Long maxUploadRateInBytes = configProvider.get().getMaxUploadRate() * 1000L;
        this.currentSpeed = (minUploadRateInBytes.equals(maxUploadRateInBytes))
                ? maxUploadRateInBytes
                : ThreadLocalRandom.current().nextLong(minUploadRateInBytes, maxUploadRateInBytes);
    }

    public long getInBytesPerSeconds() {
        return this.currentSpeed;
    }

}
