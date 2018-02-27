package org.araymond.joal.core.bandwith;

import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RandomSpeedProviderTest {
    private static final long MIN_UPLOAD_RATE = 100L;
    private static final long MAX_UPLOAD_RATE = 200L;

    private AppConfiguration mockedConf() {
        final AppConfiguration conf = mock(AppConfiguration.class);
        Mockito.doReturn(MIN_UPLOAD_RATE).when(conf).getMinUploadRate();
        Mockito.doReturn(MAX_UPLOAD_RATE).when(conf).getMaxUploadRate();

        return conf;
    }

    @Test
    public void shouldProvideValueWithinRange() {
        final RandomSpeedProvider speedProvider = new RandomSpeedProvider(this.mockedConf());
        for (int i = 0; i < 50; i++) {
            speedProvider.refresh();
            assertThat(speedProvider.getInBytesPerSeconds() / 1000).isBetween(MIN_UPLOAD_RATE, MAX_UPLOAD_RATE);
        }
    }

    @Test
    public void shouldReturnRandomSpeedAfterBuild() {
        // no call to refresh before call to get
        final RandomSpeedProvider speedProvider = new RandomSpeedProvider(this.mockedConf());

        assertThat(speedProvider.getInBytesPerSeconds() / 1000).isBetween(MIN_UPLOAD_RATE, MAX_UPLOAD_RATE);
    }

    @Test
    public void shouldRefreshSpeed() {
        final RandomSpeedProvider speedProvider = new RandomSpeedProvider(this.mockedConf());

        final Set<Long> recordedSpeeds = IntStream.range(1, 10)
                .mapToObj(i -> {
                    speedProvider.refresh();
                    return speedProvider.getInBytesPerSeconds();
                })
                .collect(Collectors.toSet());

        assertThat(recordedSpeeds.size()).isGreaterThan(1);
    }

}
