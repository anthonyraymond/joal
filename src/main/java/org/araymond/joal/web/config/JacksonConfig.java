package org.araymond.joal.web.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by raymo on 30/06/2017.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .featuresToDisable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                )
                .failOnEmptyBeans(false)
                .serializers(
                        new InfoHashSerializer(),
                        new LocalDateTimeSerializer()
                );
    }

    /**
     * Forces the timezone to be added to every LocalDateTime
     */
    public static final class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(final LocalDateTime value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
            final ZonedDateTime zonedDateTime = value.atZone(ZoneId.systemDefault());
            serializers.defaultSerializeValue(zonedDateTime, gen);
        }

        @Override
        public Class<LocalDateTime> handledType() {
            return LocalDateTime.class;
        }
    }

    public static final class InfoHashSerializer extends JsonSerializer<InfoHash> {
        @Override
        public void serialize(final InfoHash value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
            gen.writeString(value.value());
        }

        @Override
        public Class<InfoHash> handledType() {
            return InfoHash.class;
        }
    }

}
