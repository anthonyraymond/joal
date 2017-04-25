package org.araymond.joal.core.client.emulated;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.HttpHeader;

/**
 * Created by raymo on 26/01/2017.
 */
public class BitTorrentClient {
    private final String peerId;
    private final String key;
    private final String query;
    private final List<Map.Entry<String, String>> headers;
    private final Integer numwant;

    public BitTorrentClient(final String peerId, final String key, final String query, final Collection<HttpHeader> headers, final Integer numwant) {
        Preconditions.checkArgument(!StringUtils.isBlank(peerId), "peerId cannot be null or empty");
        if (key != null) {
            Preconditions.checkArgument(key.trim().length() != 0, "key can be null but must not be empty");
        }
        Preconditions.checkArgument(!StringUtils.isBlank(query), "query cannot be null or empty");
        Preconditions.checkNotNull(headers, "headers cannot be null");
        Preconditions.checkNotNull(numwant, "numwant cannot be null");
        Preconditions.checkArgument(numwant > 0, "numwant must be greater than 0");
        this.peerId = peerId;
        this.query = query;
        this.headers = headers.stream().map(h -> new AbstractMap.SimpleImmutableEntry<>(h.getName(), h.getValue())).collect(Collectors.toList());
        this.key = key;
        this.numwant = numwant;
    }

    public String getPeerId() {
        return peerId;
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    public String getQuery() {
        return query;
    }

    public List<Map.Entry<String, String>> getHeaders() {
        return ImmutableList.copyOf(headers);
    }

    public Integer getNumwant() {
        return numwant;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BitTorrentClient that = (BitTorrentClient) o;
        return com.google.common.base.Objects.equal(peerId, that.peerId) &&
                Objects.equal(key, that.key) &&
                Objects.equal(query, that.query) &&
                Objects.equal(headers, that.headers) &&
                Objects.equal(numwant, that.numwant);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(peerId, key, query, headers, numwant);
    }
}
