package org.araymond.joal.core.client.emulated;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.stream.Collectors;

import static org.araymond.joal.core.client.emulated.TorrentClientConfig.HttpHeader;

/**
 * Created by raymo on 26/01/2017.
 */
public class EmulatedClient {
    private final String peerId;
    private final String key;
    private final String query;
    private final List<Map.Entry<String, String>> headers;
    private final Integer numwant;

    public EmulatedClient(final String peerId, final String key, final String query, final Collection<HttpHeader> headers, final Integer numwant) {
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

    public Optional<Integer> getNumwant() {
        return Optional.ofNullable(numwant);
    }
}
