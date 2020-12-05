package com.github.cache.crypto.impl;

import com.github.cache.annotations.Encryption;
import com.github.cache.crypto.AESEncryptionService;
import com.github.cache.models.EncryptionMode;
import com.github.cache.models.Version;
import com.google.inject.Singleton;

@Singleton
@Encryption(mode = EncryptionMode.AES, version = Version.V1)
public class AESEncryptionServiceV1 extends AESEncryptionService {

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";

    private static final int KEY_LENGTH_BIT = 128; // must be one of {128, 120, 112, 104, 96}
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;

    public AESEncryptionServiceV1(String password) {
        super(ENCRYPT_ALGO, KEY_LENGTH_BIT, IV_LENGTH_BYTE, SALT_LENGTH_BYTE, password);
    }

}
