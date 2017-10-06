package org.araymond.joal.core.client.emulated.generator.key.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.Test;

import java.io.IOException;

import static org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes.HASH;
import static org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes.HASH_NO_LEADING_ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class KeyTypeSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldSerializeAlphabetic() throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(HASH)).isEqualTo("\"hash\"");
    }

    @Test
    public void shouldDeserializeAlphabetic() throws IOException {
        final KeyTypes type = mapper.readValue("\"hash\"", KeyTypes.class);
        assertThat(type).isEqualTo(HASH);
    }

    @Test
    public void shouldSerializeNumeric() throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(HASH_NO_LEADING_ZERO)).isEqualTo("\"hash_no_leading_zero\"");
    }

    @Test
    public void shouldDeserializeNumeric() throws IOException {
        final KeyTypes type = mapper.readValue("\"hash_no_leading_zero\"", KeyTypes.class);
        assertThat(type).isEqualTo(HASH_NO_LEADING_ZERO);
    }

    @Test
    public void shouldFailToDeserializeWithNonExistingValue() {
        assertThatThrownBy(() -> mapper.readValue("\"oops\"", KeyTypes.class))
                .isInstanceOf(InvalidFormatException.class)
                .hasMessageContaining("value not one of declared Enum instance names: [hash_no_leading_zero, hash]");

    }

}
