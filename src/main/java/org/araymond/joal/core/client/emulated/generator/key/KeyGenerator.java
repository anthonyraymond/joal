package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.StringTypes;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 16/07/2017.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="refreshOn")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NeverRefreshKeyGenerator.class, name = "NEVER"),
        @JsonSubTypes.Type(value = AlwaysRefreshKeyGenerator.class, name = "ALWAYS"),
        @JsonSubTypes.Type(value = TimedRefreshKeyGenerator.class, name = "TIMED"),
        @JsonSubTypes.Type(value = TorrentVolatileRefreshKeyGenerator.class, name = "TORRENT_VOLATILE"),
        @JsonSubTypes.Type(value = TorrentPersistentRefreshKeyGenerator.class, name = "TORRENT_PERSISTENT")
})
public abstract class KeyGenerator {

    private final Integer length;
    private final StringTypes type;
    private final boolean upperCase;
    private final boolean lowerCase;

    protected KeyGenerator(final Integer length, final StringTypes type, final boolean upperCase, final boolean lowerCase) {
        if (length <=0) {
            throw new TorrentClientConfigIntegrityException("key length must be greater than 0.");
        }
        if (type == null) {
            throw new TorrentClientConfigIntegrityException("key type must not be null.");
        }
        this.length = length;
        this.type = type;
        this.upperCase = upperCase;
        this.lowerCase = lowerCase;
    }

    @JsonProperty("length")
    Integer getLength() {
        return length;
    }

    @JsonProperty("type")
    StringTypes getType() {
        return type;
    }

    @JsonProperty("upperCase")
    boolean isUpperCase() {
        return upperCase;
    }

    @JsonProperty("lowerCase")
    boolean isLowerCase() {
        return lowerCase;
    }

    @JsonIgnore
    public abstract String getKey(final MockedTorrent torrent, RequestEvent event);

    protected String generateKey() {
        String key = this.type.generateString(this.length);
        if (this.upperCase) {
            key = key.toUpperCase();
        } else if (this.lowerCase) {
            key = key.toLowerCase();
        }

        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final KeyGenerator keyGenerator = (KeyGenerator) o;
        return upperCase == keyGenerator.upperCase &&
                lowerCase == keyGenerator.lowerCase &&
                com.google.common.base.Objects.equal(length, keyGenerator.length) &&
                type == keyGenerator.type;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(length, type, upperCase, lowerCase);
    }

}
