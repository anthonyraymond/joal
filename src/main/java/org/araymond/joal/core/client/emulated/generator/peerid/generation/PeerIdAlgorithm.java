package org.araymond.joal.core.client.emulated.generator.peerid.generation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegexPatternPeerIdAlgorithm.class, name = "REGEX"),
        @JsonSubTypes.Type(value = RandomPoolWithChecksumPeerIdAlgorithm.class, name = "RANDOM_POOL_WITH_CHECKSUM")
})
public interface PeerIdAlgorithm {

    String generate();

}
