package com.github.cache;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.cache.annotations.Cache;
import com.github.cache.crypto.CryptoFactory;
import com.github.cache.models.EncryptionMode;
import com.github.cache.models.StoredCache;
import com.github.cache.storage.StoredCacheDao;
import com.github.cache.utils.CompressionUtils;
import com.github.cache.utils.Constants;
import com.github.cache.utils.Utils;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Named;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class CachingModule extends AbstractModule {

    private StoredCacheDao storedCacheDao;

    @Override
    protected void configure() {
        final CacheInterceptor cacheInterceptor = new CacheInterceptor();
        if (storedCacheDao != null) {
            bind(StoredCacheDao.class).toInstance(storedCacheDao);
        }
        requestInjection(cacheInterceptor);
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

            val cache = invocation.getMethod().getAnnotation(Cache.class);
            String key, groupingKey;
            try {
                val documentContext = Utils.getDocumentContext(invocation);
                key = Utils.buildKey(documentContext, cache.keys());
                groupingKey = Utils.buildKey(documentContext, cache.groupingKeys());
            } catch (Exception e) {
                log.error("Error forming Key. Please check cache param. Method Name: {}", invocation.getMethod().getName(), e);
                return invocation.proceed();
            }

            try {
                val cachedResponse = getResponseObject(invocation, cache, groupingKey, key);
                if (Objects.nonNull(cachedResponse)) {
                    return cachedResponse;
                }
            } catch (Exception e) {
                log.error("Error getting value from cache for Key:{}, Method Name: {}", key, invocation.getMethod().getName(), e);
                return invocation.proceed();
            }

            return saveAndReturnResponse(invocation, cache, groupingKey, key);
        }

        private Object getResponseObject(MethodInvocation invocation, Cache cache, String groupingKey, String key) throws Exception {
            val storedCache = storedCacheDao.get(groupingKey, key).orElse(null);
            if (storedCache == null) {
                return null;
            }
            if (!isValid(storedCache, cache)) {
                log.info("Cache is expired for Key :: {}. Hence removing data and proceeding invocation", key);
                storedCacheDao.remove(groupingKey, key);
                return null;
            }
            val decryptedData = decryptIfRequired(storedCache);
            return decodeAndTransform(invocation, decryptedData);
        }

        private Object decodeAndTransform(MethodInvocation invocation, byte[] decryptedData) throws java.io.IOException {
            if (invocation.getMethod().getReturnType().equals(invocation.getMethod().getGenericReturnType())) {
                return CompressionUtils.decode(decryptedData, invocation.getMethod().getReturnType());
            } else {
                val javaType = TypeFactory.defaultInstance()
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

        private Object saveAndReturnResponse(MethodInvocation invocation, Cache cache, String groupingKey, String key) throws Throwable {
            val response = invocation.proceed();
            try {
                val data = encodeAndEncryptIfNecessary(response, cache);
                storedCacheDao.save(data, groupingKey, key, cache.ttlInSec());
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
    }
}
