package org.araymond.joalcore.core.infohash.domain;

import org.araymond.joalcore.annotations.ddd.ValueObject;

import java.util.Arrays;

@ValueObject
public record InfoHash(byte[] bytes) {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    String hexInfoHash() {
        char[] hexChars = new char[this.bytes.length * 2];
        for (int j = 0; j < this.bytes.length; j++) {
            int v = this.bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InfoHash) obj;
        return Arrays.equals(this.bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return "InfoHash[" +
                "bytes=" + Arrays.toString(bytes) + ']';
    }

}
