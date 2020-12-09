package com.github.cache;

import com.github.cache.annotations.Cache;
import com.github.cache.crypto.EncryptionService;
import com.github.cache.models.EncryptionMeta;
import com.github.cache.models.EncryptionMode;
import com.github.cache.models.StoredCache;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import lombok.val;

import java.util.List;

public class DummyClass {
    private final EncryptionService encryptionService;

    @Inject
    public DummyClass(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @SneakyThrows
    StoredCache storedCacheMethodWithoutAnnotation(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }

    @SneakyThrows
    @Cache(keys = {"$.userId", "$.encryptionMode", "method"})
    StoredCache storedCacheMethod(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }

    @SneakyThrows
    @Cache(keys = {"$.userId"})
    List<StoredCache> storedCacheListMethod(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return ImmutableList.of(
                StoredCache.builder()
                        .data(data)
                        .encryptionMeta(EncryptionMeta.builder()
                                .encryptionMode(encryptionMode)
                                .build())
                        .build(),
                StoredCache.builder()
                        .data(data)
                        .encryptionMeta(EncryptionMeta.builder()
                                .encryptionMode(encryptionMode)
                                .build())
                        .build()
        );
    }

    @SneakyThrows
    @Cache(keys = {"$.userId", "$.encryptionMode"}, encrypt = true, ttlInSec = 100)
    StoredCache storedCacheMethodWithEncryption(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }

    @SneakyThrows
    @Cache(keys = {"$.userId", "$.encryptionMode", "method"}, structureChangeAt = 1607433312525L, ttlInSec = 150)
    StoredCache storedCacheMethodWithStructureChange(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }

    @SneakyThrows
    @Cache(keys = {"$.userId", "$.mode", "method"})
    StoredCache methodWithInvalidKey(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }

    @SneakyThrows
    @Cache(keys = {"$.userId"})
    void voidMethod(String userId) {
        encryptionService.encrypt(userId.getBytes());
    }

}
