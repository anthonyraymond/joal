package org.araymond.joal.core.ttorent.client.announce;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 04/07/2017.
 */
public class AnnounceResultSuccessTest {

    @Test
    public void shouldBuild() {
        final AnnounceResult.SuccessAnnounceResult announceResult = new AnnounceResult.SuccessAnnounceResult();
        assertThat(announceResult.getType()).isEqualTo(AnnounceResult.AnnounceResultType.SUCCESS);
    }

}
