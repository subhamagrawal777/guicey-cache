package com.github.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.cache.annotations.Cache;
import com.github.cache.crypto.CryptoFactory;
import com.github.cache.models.EncryptionMode;
import com.github.cache.models.StoredCache;
import com.github.cache.storage.StoredCacheDao;
import com.github.cache.utils.CompressionUtils;
import com.github.cache.utils.Constants;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Named;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class CachingModule extends AbstractModule {

    private Provider<StoredCacheDao> storedCacheDaoProvider;

    @Override
    protected void configure() {
        final CacheInterceptor cacheInterceptor = new CacheInterceptor();
        requestInjection(cacheInterceptor);
        bind(StoredCacheDao.class).toProvider(storedCacheDaoProvider);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Cache.class), cacheInterceptor);
    }

    //TODO:: Should I ask from Client? Then would they ensure that they send the same every time...
    // Probably it should reside encrypted in the bundle itself
    @Provides
    @Named("AES_V1_PASSWORD")
    public String provideAESPassword() {
        return "temporary_password";
    }

    public class CacheInterceptor implements MethodInterceptor {

        @Inject
        private StoredCacheDao storedCacheDao;

        @Inject
        private CryptoFactory cryptoFactory;

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if (Void.TYPE.equals(invocation.getMethod().getReturnType())) {
                return invocation.proceed();
            }

            Cache cache = invocation.getMethod().getAnnotation(Cache.class);
            String key;
            try {
                key = getKey(cache, invocation);
            } catch (Exception e) {
                log.error("Error forming Key. Please check cache param. Method Name: {}", invocation.getMethod().getName(), e);
                return invocation.proceed();
            }

            try {
                val cachedResponse = getResponseObject(invocation, cache, key);
                if (Objects.nonNull(cachedResponse)) {
                    return cachedResponse;
                }
            } catch (Exception e) {
                log.error("Error getting value from cache for Key:{}, Method Name: {}", key, invocation.getMethod().getName(), e);
                return invocation.proceed();
            }

            return saveAndReturnResponse(invocation, cache, key);
        }

        private Object getResponseObject(MethodInvocation invocation, Cache cache, String key) throws Exception {
            val storedCache = storedCacheDao.get(key).orElse(null);
            if (storedCache == null) {
                return null;
            }
            if (!isValid(storedCache, cache)) {
                storedCacheDao.remove(key);
                return null;
            }
            val decryptedData = decryptIfRequired(storedCache);
            return decodeAndTransform(invocation, decryptedData);
        }

        private Object decodeAndTransform(MethodInvocation invocation, byte[] decryptedData) throws java.io.IOException {
            if (invocation.getMethod().getReturnType().equals(invocation.getMethod().getGenericReturnType())) {
                return CompressionUtils.decode(decryptedData, invocation.getMethod().getReturnType());
            } else {
                JavaType javaType = TypeFactory.defaultInstance()
                        .constructType(invocation.getMethod().getGenericReturnType());
                return CompressionUtils.decode(decryptedData, javaType);
            }
        }

        private boolean isValid(StoredCache storedCache, Cache cache) {
            return storedCache.getCachedAt() > cache.structureChangeAt();
        }

        private byte[] decryptIfRequired(StoredCache storedCache) throws Exception {
            if (storedCache.getEncryptionMeta() != null &&
                    storedCache.getEncryptionMeta().getEncryptionMode() != EncryptionMode.NONE) {
                return cryptoFactory.getEncryptionService(storedCache.getEncryptionMeta())
                        .decrypt(storedCache.getData());
            }
            return storedCache.getData();
        }

        private Object saveAndReturnResponse(MethodInvocation invocation, Cache cache, String key) throws Throwable {
            val response = invocation.proceed();
            try {
                val data = encodeAndEncryptIfNecessary(response, cache);
                storedCacheDao.save(data, key, cache.ttlInSec());
            } catch (Exception e) {
                log.error("Error saving in Cache for key: {}, Method Name: {}", key, invocation.getMethod().getName(), e);
            }
            return response;
        }

        private StoredCache encodeAndEncryptIfNecessary(Object responseData, Cache cache) throws Exception {
            val encodedData = CompressionUtils.encode(responseData);
            val finalData = cache.encrypt()
                    ? cryptoFactory.getDefaultEncryptionService().encrypt(encodedData)
                    : encodedData;
            val encryptionMeta = cache.encrypt() ? Constants.DEFAULT_ENCRYPTION_META : null;
            return StoredCache.builder()
                    .data(finalData)
                    .cachedAt(Instant.now().toEpochMilli())
                    .encryptionMeta(encryptionMeta)
                    .build();
        }

        private String getKey(Cache cache, MethodInvocation invocation) {
            val context = Maps.newHashMap();
            for (int i = 0; i < invocation.getMethod().getParameters().length; i++) {
                context.put(invocation.getMethod().getParameters()[i].getName(), invocation.getArguments()[i]);
            }
            val documentContext = JsonPath.parse(context);

            return Arrays.stream(cache.keys())
                    .map(key -> {
                        if (key.startsWith("$.")) {
                            val result = documentContext.read(key, String.class);
                            Preconditions.checkNotNull(result);
                            return result;
                        }
                        return key;
                    })
                    .collect(Collectors.joining(":"));
        }
    }
}
