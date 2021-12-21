package org.araymond.joal.conf;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import static org.assertj.core.api.Assertions.fail;

public class SpringConfTest {

    @Test
    public void shouldCreateExecutor() {
        final SpringConf springConf = new SpringConf();

        try {
            final TaskExecutor taskExecutor = springConf.taskExecutor();
        } catch (final Exception e) {
            fail("should not have failed");
        }
    }

}
