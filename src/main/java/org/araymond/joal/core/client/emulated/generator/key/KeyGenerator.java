package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

/**
 * Created by raymo on 16/07/2017.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="refreshOn")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NeverRefreshKeyGenerator.class, name = "NEVER"),
        @JsonSubTypes.Type(value = AlwaysRefreshKeyGenerator.class, name = "ALWAYS"),
        @JsonSubTypes.Type(value = TimedRefreshKeyGenerator.class, name = "TIMED"),
        @JsonSubTypes.Type(value = TimedOrAfterStartedAnnounceRefreshKeyGenerator.class, name = "TIMED_OR_AFTER_STARTED_ANNOUNCE"),
        @JsonSubTypes.Type(value = TorrentVolatileRefreshKeyGenerator.class, name = "TORRENT_VOLATILE"),
        @JsonSubTypes.Type(value = TorrentPersistentRefreshKeyGenerator.class, name = "TORRENT_PERSISTENT")
})
public abstract class KeyGenerator {

    private final KeyAlgorithm algorithm;
    private final Casing keyCase;

    protected KeyGenerator(final KeyAlgorithm keyAlgorithm, final Casing keyCase) {
        if (keyAlgorithm == null) {
            throw new TorrentClientConfigIntegrityException("key algorithm must not be null.");
        }
        this.algorithm = keyAlgorithm;
        this.keyCase = keyCase;
    }


    @JsonProperty("algorithm")
    KeyAlgorithm getAlgorithm() {
        return algorithm;
    }

    @JsonProperty("keyCase")
    Casing getKeyCase() {
        return keyCase;
    }


    @JsonIgnore
    public abstract String getKey(final MockedTorrent torrent, RequestEvent event);

    protected String generateKey() {
        final String key = this.algorithm.generate();

        return keyCase.toCase(key);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final KeyGenerator keyGenerator = (KeyGenerator) o;
        return keyCase == keyGenerator.keyCase &&
                com.google.common.base.Objects.equal(algorithm, keyGenerator.algorithm);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(algorithm, keyCase);
    }

}
