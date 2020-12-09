package com.github.cache.crypto;

public interface EncryptionService {
    byte[] encrypt(byte[] input) throws Exception;

    byte[] decrypt(byte[] input) throws Exception;
}
