package com.github.cache.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
@BindingAnnotation
public @interface Cache {
    /**
     * The Final Key is formed by evaluating each key either from the params or taking the key itself
     * and finally joined by delimiter ':'.
     *
     * If the key starts with "$." then it is evaluated against the params. And if evaluation failed or returned null,
     * then the whole steps are ignored and normal invocation is called
     */
    String[] keys();

    int ttlInSec() default 600;

    boolean encrypt() default false;
}
