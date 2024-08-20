package org.araymond.joalcore.annotations.ddd;

import org.araymond.joalcore.annotations.concurency.Immutable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Immutable
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ValueObject {

}
