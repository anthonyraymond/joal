package org.araymond.joal.core.client.emulated.generator.peerid.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;

import java.security.SecureRandom;
import java.time.Instant;

public class RandomPoolWithChecksumPeerIdAlgorithm implements PeerIdAlgorithm {

    private final SecureRandom random;
    private Integer refreshSeedAfter;
    private Integer generationCount;
    private final String prefix;
    private final String charactersPool;
    private final Integer base;

    public RandomPoolWithChecksumPeerIdAlgorithm(
            @JsonProperty(value = "prefix", required = true) final String prefix,
            @JsonProperty(value = "charactersPool", required = true) final String charactersPool,
            @JsonProperty(value = "base", required = true) final Integer base
    ) {
        if (StringUtils.isBlank(prefix)) {
            throw new TorrentClientConfigIntegrityException("peerId algorithm prefix must not be null or empty.");
        }

        if (StringUtils.isBlank(charactersPool)) {
            throw new TorrentClientConfigIntegrityException("peerId algorithm charactersPool must not be null or empty.");
        }

        if (base == null) {
            throw new TorrentClientConfigIntegrityException("peerId algorithm base must not be null.");
        }
        this.random = new SecureRandom(createSecureRandomSeed());
        this.refreshSeedAfter = getRandomIntBetween10And50();
        this.generationCount = 0;
        this.prefix = prefix;
        this.charactersPool = charactersPool;
        this.base = base;
    }

    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    @JsonProperty("charactersPool")
    public String getCharactersPool() {
        return charactersPool;
    }

    @JsonProperty("base")
    public Integer getBase() {
        return base;
    }

    private byte[] createSecureRandomSeed() {
        return Instant.now().toString().getBytes();
    }

    private Integer getRandomIntBetween10And50() {
        // Using the current random to generate another random would be completely useless because if the SecureRandom appears to be predictable we will be able to predict the next int as well
        int randNumber = new SecureRandom(createSecureRandomSeed()).nextInt();
        randNumber = Math.abs(randNumber % 40);
        return (randNumber + 10);
    }

    @Override
    public String generate() {
        // This test is subject to multi-thread issues, but in this case it's actually a good news
        if (this.generationCount >= this.refreshSeedAfter) {
            // Times to times we reset the seed to enforce randomness
            this.generationCount = 0;
            this.random.setSeed(createSecureRandomSeed());
            this.refreshSeedAfter = getRandomIntBetween10And50();
        }

        this.generationCount += 1;

        final int suffixLength = PeerIdGenerator.PEER_ID_LENGTH - this.prefix.length();
        final byte[] randomBytes = new byte[suffixLength - 1];
        final char[] buf = new char[suffixLength];
        int val, total = 0;

        this.random.nextBytes(randomBytes);

        for (int i = 0; i < 11; ++i)
        {
            val = (randomBytes[i] + 128) % this.base;
            total += val;
            buf[i] = this.charactersPool.charAt(val);
        }
        val = (total % this.base) != 0 ? this.base - (total % this.base) : 0;
        buf[11] = this.charactersPool.charAt(val);
        return this.prefix + new String(buf);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RandomPoolWithChecksumPeerIdAlgorithm that = (RandomPoolWithChecksumPeerIdAlgorithm) o;
        return Objects.equal(prefix, that.prefix) &&
                Objects.equal(charactersPool, that.charactersPool) &&
                Objects.equal(base, that.base);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(prefix, charactersPool, base);
    }
}
