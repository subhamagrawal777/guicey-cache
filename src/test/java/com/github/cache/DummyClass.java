package com.github.cache;

import com.github.cache.annotations.Cache;
import com.github.cache.annotations.Index;
import com.github.cache.annotations.RemoveCache;
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
    public StoredCache storedCacheMethodWithoutAnnotation(String userId, EncryptionMode encryptionMode) {
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
    public StoredCache storedCacheMethod(String userId, EncryptionMode encryptionMode) {
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
    public List<StoredCache> storedCacheListMethod(String userId, EncryptionMode encryptionMode) {
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
    public StoredCache storedCacheMethodWithEncryption(String userId, EncryptionMode encryptionMode) {
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
    public StoredCache storedCacheMethodWithStructureChange(String userId, EncryptionMode encryptionMode) {
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
    public StoredCache methodWithInvalidKey(String userId, EncryptionMode encryptionMode) {
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
    public void voidMethod(String userId) {
        encryptionService.encrypt(userId.getBytes());
    }

    @SneakyThrows
    @RemoveCache(keySet = {
            @Index(groupingKeys = "TEST"),
            @Index(groupingKeys = {"$.userId", "TEMP"}, keys = {"$.encryptionMode", "TEMP"}),
            @Index(groupingKeys = {"TEST"}, keys = {"$.userId", "$.mo"})
    }, before = true)
    public StoredCache methodWithBothFlagsTurnedOn(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }

    @SneakyThrows
    @RemoveCache(keySet = {
            @Index(groupingKeys = "TEST"),
            @Index(groupingKeys = {"$.userId", "TEMP"}, keys = {"$.encryptionMode", "TEMP"}),
            @Index(groupingKeys = {"TEST"}, keys = {"$.userId", "$.mo"})
    }, after = false)
    public StoredCache methodWithBothFlagsTurnedOff(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }

    @SneakyThrows
    @RemoveCache(keySet = {
            @Index(groupingKeys = "TEST"),
            @Index(groupingKeys = {"$.userId", "TEMP"}, keys = {"$.encryptionMode", "TEMP"}),
            @Index(groupingKeys = {"TEST"}, keys = {"$.userId", "$.mo"})
    })
    public StoredCache methodWithOneInvalidIndex(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }

    @SneakyThrows
    @RemoveCache(keySet = {
            @Index(groupingKeys = "$.userrId"),
            @Index(groupingKeys = {"TEST", "$.user"}, keys = {"$.userId", "$.mode"}),
            @Index(groupingKeys = {"TEST"}, keys = {"$.userId", "$.mo"})
    })
    public StoredCache methodWithAllInvalidIndices(String userId, EncryptionMode encryptionMode) {
        val data = encryptionService.encrypt(userId.getBytes());
        return StoredCache.builder()
                .data(data)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(encryptionMode)
                        .build())
                .build();
    }
}
