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

    /**
     * Would be used to bucket a set of cache and later remove the whole set underneath the group
     */
    String[] groupingKeys() default {"default-cache-set"};

    int ttlInSec() default 600;

    boolean encrypt() default false;

    /**
     * If your method structure has changed or there has been some additions in the response object.
     * In the above cases, you don't want to use cached data then you can specify the timestamp in millis when the change was deployed.
     *
     * If the cached object was before the given time, it would force call the method
     */
    long structureChangeAt() default 0;
}
