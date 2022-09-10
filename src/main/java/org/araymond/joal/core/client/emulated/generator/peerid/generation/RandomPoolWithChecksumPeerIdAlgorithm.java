package org.araymond.joal.core.client.emulated.generator.peerid.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;

import java.security.SecureRandom;
import java.time.Instant;

@EqualsAndHashCode(of = {"prefix", "charactersPool", "base"})
public class RandomPoolWithChecksumPeerIdAlgorithm implements PeerIdAlgorithm {

    private final SecureRandom random;
    private int refreshSeedAfter;
    private int generationCount;

    @JsonProperty("prefix")
    @Getter
    private final String prefix;
    @JsonProperty("charactersPool")
    @Getter
    private final String charactersPool;
    @JsonProperty("base")
    @Getter
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

    @VisibleForTesting
    byte[] createSecureRandomSeed() {
        return Instant.now().toString().getBytes(Charsets.UTF_8);
    }

    private int getRandomIntBetween10And50() {
        // Using the current random to generate another random would be completely useless because if the SecureRandom appears to be predictable we will be able to predict the next int as well
        int randNumber = new SecureRandom(createSecureRandomSeed()).nextInt();
        randNumber = Math.abs(randNumber % 40);
        return randNumber + 10;
    }

    @VisibleForTesting
    byte[] generateRandomBytes(final int length) {
        // This test is subject to multi-thread issues, but in this case it's actually a good news
        if (this.generationCount >= this.refreshSeedAfter) {
            // Times to times we reset the seed to enforce randomness
            this.generationCount = 0;
            this.random.setSeed(createSecureRandomSeed());
            this.refreshSeedAfter = getRandomIntBetween10And50();
        }

        this.generationCount += 1;

        final byte[] bytes = new byte[length];
        this.random.nextBytes(bytes);
        return bytes;
    }

    @Override
    public String generate() {
        final int suffixLength = PeerIdGenerator.PEER_ID_LENGTH - this.prefix.length();
        final byte[] randomBytes = this.generateRandomBytes(suffixLength - 1);
        final char[] buf = new char[suffixLength];
        int val, total = 0;

        for (int i = 0; i < suffixLength - 1; ++i) {
            val = randomBytes[i] < 0 ? randomBytes[i] + 256 : randomBytes[i];
            val %= base;
            total += val;
            buf[i] = this.charactersPool.charAt(val);
        }
        val = (total % this.base) != 0 ? this.base - (total % this.base) : 0;
        buf[suffixLength - 1] = this.charactersPool.charAt(val);
        return this.prefix + new String(buf);
    }
}
