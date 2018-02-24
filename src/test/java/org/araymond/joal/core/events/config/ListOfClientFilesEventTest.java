package org.araymond.joal.core.events.config;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ListOfClientFilesEventTest {

    @Test
    public void shouldBuild() {
        final List<String> clientFiles = Lists.newArrayList();
        final ListOfClientFilesEvent event = new ListOfClientFilesEvent(clientFiles);

        assertThat(event.getClients()).isEqualTo(clientFiles);
    }

}
