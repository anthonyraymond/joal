package org.araymond.joal.core.client.emulated.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Casing {
    @JsonProperty("upper")
    UPPER,
    @JsonProperty("lower")
    LOWER,
    @JsonProperty("none")
    NONE;

    public String toCase(final String str) {
        final String value;
        switch (this) {
            case UPPER:
                value = str.toUpperCase();
                break;
            case LOWER:
                value = str.toLowerCase();
                break;
            case NONE:
                value = str;
                break;
            default:
                throw new IllegalStateException("Unhandled type: " + this.name());
        }
        return value;
    }
}
