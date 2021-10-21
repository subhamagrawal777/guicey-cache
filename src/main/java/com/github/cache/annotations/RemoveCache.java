package com.github.cache.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
@BindingAnnotation
public @interface RemoveCache {

    Index[] keySet();

    boolean before() default false;

    boolean after() default true;
}
