package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

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

    private final Integer length;
    private final KeyTypes type;
    private final Casing keyCase;

    protected KeyGenerator(final Integer length, final KeyTypes type, final Casing keyCase) {
        if (length <=0) {
            throw new TorrentClientConfigIntegrityException("key length must be greater than 0.");
        }
        if (type == null) {
            throw new TorrentClientConfigIntegrityException("key type must not be null.");
        }
        this.length = length;
        this.type = type;
        this.keyCase = keyCase;
    }

    @JsonProperty("length")
    Integer getLength() {
        return length;
    }

    @JsonProperty("type")
    KeyTypes getType() {
        return type;
    }

    @JsonProperty("keyCase")
    Casing getKeyCase() {
        return keyCase;
    }


    @JsonIgnore
    public abstract String getKey(final MockedTorrent torrent, RequestEvent event);

    protected String generateKey() {
        final String key = this.type.generateHash(this.length);

        return keyCase.toCase(key);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final KeyGenerator keyGenerator = (KeyGenerator) o;
        return keyCase == keyGenerator.keyCase &&
                com.google.common.base.Objects.equal(length, keyGenerator.length) &&
                type == keyGenerator.type;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(length, type, keyCase);
    }

}
