package org.araymond.joal.core.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class AppConfigurationTest {

    public static AppConfiguration createOne() {
        return new AppConfiguration(30L, 150L, 2, "azureus", true, 1f);
    }

    @Test
    public void shouldNotBuildIfMinUploadRateIsLessThanZero() {
        assertThatThrownBy(() -> new AppConfiguration(-1L, 190L, 2, "azureus.client", false, 1f))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("minUploadRate must be at least 0");
    }

    @Test
    public void shouldBuildIfMinUploadRateEqualsZero() {
        final AppConfiguration config = new AppConfiguration(0L, 190L, 2, "azureus.client", false, 1f);

        assertThat(config.getMinUploadRate()).isEqualTo(0);
    }

    @Test
    public void shouldBuildIfMinUploadRateEqualsOne() {
        final AppConfiguration config = new AppConfiguration(0L, 1L, 2, "azureus.client", false, 1f);

        assertThat(config.getMaxUploadRate()).isEqualTo(1);
    }

    @Test
    public void shouldNotBuildIfMaxUploadRateIsLessThanZero() {
        assertThatThrownBy(() -> new AppConfiguration(180L, -1L, 2, "azureus.client", false, 1f))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("maxUploadRate must greater or equal to 0");
    }

    @Test
    public void shouldBuildIfMinRateAndMaxRateEqualsZero() {
        final AppConfiguration conf = new AppConfiguration(0L, 0L, 2, "azureus.client", false, 1f);

        assertThat(conf.getMinUploadRate()).isEqualTo(0L);
        assertThat(conf.getMaxUploadRate()).isEqualTo(0L);
    }

    @Test
    public void shouldNotBuildIfMaxRateIsLesserThanMinRate() {
        assertThatThrownBy(() -> new AppConfiguration(180L, 179L, 2, "azureus.client", false, 1f))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("maxUploadRate must be greater or equal to minUploadRate");
    }

    @Test
    public void shouldBuildIfMaxRateEqualsMinRate() {
        final AppConfiguration conf = new AppConfiguration(180L, 180L, 2, "azureus.client", false, 1f);

        assertThat(conf.getMinUploadRate()).isEqualTo(180L);
        assertThat(conf.getMaxUploadRate()).isEqualTo(180L);
    }

    @Test
    public void shouldNotBuildIfSimultaneousSeedIsLessThanOne() {
        assertThatThrownBy(() -> new AppConfiguration(180L, 190L, 0, "azureus.client", false, 1f))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("simultaneousSeed must be greater than 0");
    }

    @Test
    public void shouldCreateIfSimultaneousSeedIsOne() {
        final AppConfiguration config = new AppConfiguration(180L, 190L, 1, "azureus.client", false, 1f);

        assertThat(config.getSimultaneousSeed()).isEqualTo(1);
    }

    @Test
    public void shouldNotBuildIfClientIsNull() {
        assertThatThrownBy(() -> new AppConfiguration(180L, 190L, 2, null, false, 1f))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("client is required, no file name given");
    }

    @Test
    public void shouldNotBuildIfClientIsEmpty() {
        assertThatThrownBy(() -> new AppConfiguration(180L, 190L, 2, "     ", false, 1f))
                .isInstanceOf(AppConfigurationIntegrityException.class)
                .hasMessageContaining("client is required, no file name given");
    }

    @Test
    public void shouldBuild() {
        final AppConfiguration config = new AppConfiguration(180L, 190L, 2, "azureus.client", false, 1f);

        assertThat(config.getMinUploadRate()).isEqualTo(180);
        assertThat(config.getMaxUploadRate()).isEqualTo(190);
        assertThat(config.getSimultaneousSeed()).isEqualTo(2);
        assertThat(config.getClient()).isEqualTo("azureus.client");
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final AppConfiguration config = new AppConfiguration(180L, 190L, 2, "azureus.client", false, 1f);
        final AppConfiguration config2 = new AppConfiguration(180L, 190L, 2, "azureus.client", false, 1f);
        assertThat(config).isEqualTo(config2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final AppConfiguration config = new AppConfiguration(180L, 190L, 2, "azureus.client", false, 1f);
        final AppConfiguration config2 = new AppConfiguration(180L, 190L, 2, "azureus.client", false, 1f);
        assertThat(config.hashCode()).isEqualTo(config2.hashCode());
    }

}
