package com.github.cache.utils;

import com.github.cache.models.EncryptionMeta;
import com.github.cache.models.EncryptionMode;
import com.github.cache.models.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    public static final EncryptionMeta DEFAULT_ENCRYPTION_META = EncryptionMeta.builder()
            .version(Version.V1)
            .encryptionMode(EncryptionMode.AES)
            .build();
}
