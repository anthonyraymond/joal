package org.araymond.joal.core.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class AppConfigurationTest {

    @Test
    public void ShouldNotBuildIfMinUploadRateIsLessThanZero() {
        assertThatThrownBy(() -> new AppConfiguration(-1, 190, 1200, 1200, "azureus.client"))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("minUploadRate must be at least 0.");
    }

    @Test
    public void shouldCreateIfMinUploadRateEqualsZero() {
        final AppConfiguration config = new AppConfiguration(0, 190, 1200, 1200, "azureus.client");

        assertThat(config.getMinUploadRate()).isEqualTo(0);
    }

    @Test
    public void ShouldNotBuildIfMaxUploadRateIsLessThanOne() {
        assertThatThrownBy(() -> new AppConfiguration(180, -1, 1200, 1200, "azureus.client"))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("maxUploadRate must greater than 0.");
    }

    @Test
    public void shouldCreateIfMinUploadRateEqualsOne() {
        final AppConfiguration config = new AppConfiguration(0, 1, 1200, 1200, "azureus.client");

        assertThat(config.getMaxUploadRate()).isEqualTo(1);
    }

    @Test
    public void ShouldNotBuildIfMaxRateIsLesserThanMinRate() {
        assertThatThrownBy(() -> new AppConfiguration(180, 150, 1200, 1200, "azureus.client"))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("maxUploadRate must be strictly greater than minUploadRate.");
    }

    @Test
    public void ShouldNotBuildIfMaxRateEqualsThanMinRate() {
        assertThatThrownBy(() -> new AppConfiguration(180, 180, 1200, 1200, "azureus.client"))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("maxUploadRate must be strictly greater than minUploadRate.");
    }

    @Test
    public void ShouldNotBuildIfSeedForIsLessThanOne() {
        assertThatThrownBy(() -> new AppConfiguration(180, 190, 0, 1200, "azureus.client"))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("seedFor must be greater than 1.");
    }

    @Test
    public void shouldCreateIfSeedForIsOne() {
        final AppConfiguration config = new AppConfiguration(180, 190, 1, 1200, "azureus.client");

        assertThat(config.getSeedFor()).isEqualTo(1);
    }

    @Test
    public void ShouldNotBuildIfWaitBetweenSeedIsLessThanOne() {
        assertThatThrownBy(() -> new AppConfiguration(180, 190, 1200, 0, "azureus.client"))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("waitBetweenSeed must be greater than 1.");
    }

    @Test
    public void shouldCreateIfWaitBetweenSeedIsOne() {
        final AppConfiguration config = new AppConfiguration(180, 190, 1200, 1, "azureus.client");

        assertThat(config.getWaitBetweenSeed()).isEqualTo(1);
    }

    @Test
    public void ShouldNotBuildIfClientIsNull() {
        assertThatThrownBy(() -> new AppConfiguration(180, 190, 1200, 1200, null))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("client is required, no file name given.");
    }

    @Test
    public void ShouldNotBuildIfClientIsEmpty() {
        assertThatThrownBy(() -> new AppConfiguration(180, 190, 1200, 1200, "     "))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("client is required, no file name given.");
    }

    @Test
    public void shouldBuild() {
        final AppConfiguration config = new AppConfiguration(180, 190, 1250, 1200, "azureus.client");

        assertThat(config.getMinUploadRate()).isEqualTo(180);
        assertThat(config.getMaxUploadRate()).isEqualTo(190);
        assertThat(config.getSeedFor()).isEqualTo(1250);
        assertThat(config.getWaitBetweenSeed()).isEqualTo(1200);
        assertThat(config.getClientFileName()).isEqualTo("azureus.client");
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final AppConfiguration config = new AppConfiguration(180, 190, 1200, 1200, "azureus.client");
        final AppConfiguration config2 = new AppConfiguration(180, 190, 1200, 1200, "azureus.client");
        assertThat(config).isEqualTo(config2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final AppConfiguration config = new AppConfiguration(180, 190, 1200, 1200, "azureus.client");
        final AppConfiguration config2 = new AppConfiguration(180, 190, 1200, 1200, "azureus.client");
        assertThat(config.hashCode()).isEqualTo(config2.hashCode());
    }

}
