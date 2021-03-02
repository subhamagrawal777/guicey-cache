package com.github.cache.annotations;

import com.github.cache.models.EncryptionMode;
import com.github.cache.models.Version;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Encryption {
    EncryptionMode mode();

    Version version();
}
