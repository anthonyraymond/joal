package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.ValueObject;

@ValueObject
public record Leechers(int count) {
}
