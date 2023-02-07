package org.araymond.joal.core.client.emulated.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Casing {
    @JsonProperty("upper")
    UPPER {
        @Override
        public String toCase(String str) {
            return str.toUpperCase();
        }
    },

    @JsonProperty("lower")
    LOWER {
        @Override
        public String toCase(String str) {
            return str.toLowerCase();
        }
    },

    @JsonProperty("none")
    NONE {
        @Override
        public String toCase(String str) {
            return str;
        }
    };

    public abstract String toCase(String str);
}
