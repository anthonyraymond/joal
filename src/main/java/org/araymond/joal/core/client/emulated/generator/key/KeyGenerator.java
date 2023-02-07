package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.InfoHash;

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
@Getter
@EqualsAndHashCode
public abstract class KeyGenerator {

    @JsonProperty("algorithm")
    private final KeyAlgorithm algorithm;
    @JsonProperty("keyCase")
    private final Casing keyCase;

    protected KeyGenerator(final KeyAlgorithm keyAlgorithm, final Casing keyCase) {
        if (keyAlgorithm == null) {
            throw new TorrentClientConfigIntegrityException("key algorithm must not be null");
        }
        this.algorithm = keyAlgorithm;
        this.keyCase = keyCase;
    }

    @JsonIgnore
    public abstract String getKey(final InfoHash infoHash, RequestEvent event);

    protected String generateKey() {
        return keyCase.toCase(this.algorithm.generate());
    }

}
