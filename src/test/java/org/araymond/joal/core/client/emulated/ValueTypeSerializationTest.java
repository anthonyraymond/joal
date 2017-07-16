package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.araymond.joal.core.client.emulated.generator.StringTypes;
import org.junit.Test;

import java.io.IOException;

import static org.araymond.joal.core.client.emulated.generator.StringTypes.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class ValueTypeSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldSerializeAlphabetic() throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(ALPHABETIC)).isEqualTo("\"alphabetic\"");
    }

    @Test
    public void shouldDeserializeAlphabetic() throws IOException {
        final StringTypes type = mapper.readValue("\"alphabetic\"", StringTypes.class);
        assertThat(type).isEqualTo(ALPHABETIC);
    }

    @Test
    public void shouldSerializeNumeric() throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(NUMERIC)).isEqualTo("\"numeric\"");
    }

    @Test
    public void shouldDeserializeNumeric() throws IOException {
        final StringTypes type = mapper.readValue("\"numeric\"", StringTypes.class);
        assertThat(type).isEqualTo(NUMERIC);
    }

    @Test
    public void shouldSerializeAlphanumeric() throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(ALPHANUMERIC)).isEqualTo("\"alphanumeric\"");
    }

    @Test
    public void shouldDeserializeAlphanumeric() throws IOException {
        final StringTypes type = mapper.readValue("\"alphanumeric\"", StringTypes.class);
        assertThat(type).isEqualTo(ALPHANUMERIC);
    }

    @Test
    public void shouldSerializeRandom() throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(RANDOM)).isEqualTo("\"random\"");
    }

    @Test
    public void shouldDeserializeRandom() throws IOException {
        final StringTypes type = mapper.readValue("\"random\"", StringTypes.class);
        assertThat(type).isEqualTo(RANDOM);
    }

    @Test
    public void shouldSerializePrintable() throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(PRINTABLE)).isEqualTo("\"printable\"");
    }

    @Test
    public void shouldDeserializePrintable() throws IOException {
        final StringTypes type = mapper.readValue("\"printable\"", StringTypes.class);
        assertThat(type).isEqualTo(PRINTABLE);
    }

    @Test
    public void shouldFailToDeserializeWithNonExistingValue() {
        assertThatThrownBy(() -> mapper.readValue("\"oops\"", StringTypes.class))
                .isInstanceOf(InvalidFormatException.class)
                .hasMessageContaining("value not one of declared Enum instance names: [random, alphabetic, alphanumeric, numeric, printable]");

    }

}
