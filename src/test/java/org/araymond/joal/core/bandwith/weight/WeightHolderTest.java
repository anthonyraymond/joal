package org.araymond.joal.core.bandwith.weight;

import org.araymond.joal.core.bandwith.Peers;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class WeightHolderTest {

    @Test
    public void shouldComputeWeight() {
        final PeersAwareWeightCalculator calculator = Mockito.spy(PeersAwareWeightCalculator.class);

        final WeightHolder<String> weightHolder = new WeightHolder<>(calculator);

        weightHolder.addOrUpdate("a", new Peers(5, 10));
        Mockito.verify(calculator, Mockito.times(1)).calculate(Matchers.eq(new Peers(5, 10)));
    }

    @Test
    public void shouldProvideWeightForTorrent() {
        final PeersAwareWeightCalculator calculator = Mockito.spy(PeersAwareWeightCalculator.class);
        Mockito
                .doReturn(22.0)
                .when(calculator).calculate(Matchers.any());

        final WeightHolder<String> weightHolder = new WeightHolder<>(calculator);

        weightHolder.addOrUpdate("a", new Peers(10, 10));
        assertThat(weightHolder.getWeightFor("a")).isEqualTo(22.0);
    }

    @Test
    public void shouldUpdateTotalWeightOnAddAndRemoveAndUpdate() {
        final PeersAwareWeightCalculator calculator = Mockito.spy(PeersAwareWeightCalculator.class);
        Mockito
                .doReturn(22.0)
                .doReturn(55.3)
                .doReturn(90.0)
                .when(calculator).calculate(Matchers.any());

        final WeightHolder<String> weightHolder = new WeightHolder<>(calculator);

        weightHolder.addOrUpdate("a", new Peers(10, 10));
        assertThat(weightHolder.getTotalWeight()).isEqualTo(22.0);
        weightHolder.addOrUpdate("b", new Peers(10, 10));
        assertThat(weightHolder.getTotalWeight()).isEqualTo(77.3);
        weightHolder.addOrUpdate("b", new Peers(20, 20));
        assertThat(weightHolder.getTotalWeight()).isEqualTo(112.0);
        weightHolder.remove("a");
        assertThat(weightHolder.getTotalWeight()).isEqualTo(90.0);
    }

    @Test
    public void shouldProvideZeroWeightForNonExistingTorrent() {
        final PeersAwareWeightCalculator calculator = Mockito.mock(PeersAwareWeightCalculator.class);

        final WeightHolder<String> weightHolder = new WeightHolder<>(calculator);

        assertThat(weightHolder.getWeightFor("q")).isEqualTo(0.0);
    }

}
