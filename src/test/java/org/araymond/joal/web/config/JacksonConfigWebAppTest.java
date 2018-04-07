package org.araymond.joal.web.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.torrent.torrent.InfoHashTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        JacksonConfig.class,
               org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class
        },
        properties = {
                "spring.main.web-environment=true"
        }
)
public class JacksonConfigWebAppTest {

    @Inject
    private ObjectMapper mapper;

    @Test
    public void shouldSerializeDateAsYYYY_MM_DD() throws JsonProcessingException {
        final String dateValue = this.mapper.writeValueAsString(LocalDate.of(2010, 12, 30));

        assertThat(dateValue).isEqualTo("\"2010-12-30\"");
    }

    @Test
    public void shouldSerializeInfoHash() throws JsonProcessingException {
        final String infohashValue = this.mapper.writeValueAsString(InfoHashTest.createOne("abcdefgh"));

        assertThat(infohashValue).isEqualTo("\"abcdefgh\"");
    }

}
