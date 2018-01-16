package org.araymond.joal.core.events.old.config;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 09/07/2017.
 */
public class ClientFilesDiscoveredEventTest {

    @Test
    public void shouldNotBuildWithoutClientList() {
        assertThatThrownBy(() -> new ClientFilesDiscoveredEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Clients list must not be null");
    }

    @Test
    public void shouldBuild() {
        final List<String> clients = Lists.newArrayList("az.client", "ut.client");
        final ClientFilesDiscoveredEvent event = new ClientFilesDiscoveredEvent(clients);

        assertThat(event.getClients()).isEqualTo(clients);
    }

}
