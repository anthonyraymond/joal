package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HashKeyAlgorithm.class, name = "HASH"),
        @JsonSubTypes.Type(value = HashNoLeadingZeroKeyAlgorithm.class, name = "HASH_NO_LEADING_ZERO"),
        @JsonSubTypes.Type(value = RegexPatternKeyAlgorithm.class, name = "REGEX")
})
public interface KeyAlgorithm {

    String generate();

}
