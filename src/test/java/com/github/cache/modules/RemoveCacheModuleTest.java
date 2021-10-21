package com.github.cache.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cache.DummyClass;
import com.github.cache.crypto.CryptoFactory;
import com.github.cache.crypto.EncryptionService;
import com.github.cache.models.EncryptionMeta;
import com.github.cache.models.EncryptionMode;
import com.github.cache.models.StoredCache;
import com.github.cache.storage.StoredCacheDao;
import com.github.cache.utils.JsonUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RemoveCacheModuleTest {
    private static final String USER_ID = "U12345";
    private static final String DEFAULT_GROUPING_KEY = "default-cache-set";
    private static final byte[] USER_ID_IN_BYTES = USER_ID.getBytes();
    private static final EncryptionMode ENCRYPTION_MODE = EncryptionMode.AES;
    private static final byte[] BYTES_DATA = "TEMP".getBytes();


    private StoredCacheDao storedCacheDao;
    private CryptoFactory cryptoFactory;
    private EncryptionService encryptionService;
    private EncryptionService defaultEncryptionService;

    private DummyClass dummyClass;

    @Before
    public void setUp() throws Exception {
        storedCacheDao = mock(StoredCacheDao.class);
        cryptoFactory = mock(CryptoFactory.class);
        encryptionService = mock(EncryptionService.class);
        defaultEncryptionService = mock(EncryptionService.class);

        Injector injector = Guice.createInjector(
                new RemoveCacheModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(EncryptionService.class).toInstance(encryptionService);
                        bind(CryptoFactory.class).toInstance(cryptoFactory);
                        bind(StoredCacheDao.class).toInstance(storedCacheDao);
                    }
                });

        dummyClass = injector.getInstance(DummyClass.class);

        when(encryptionService.encrypt(USER_ID_IN_BYTES)).thenReturn(BYTES_DATA);

        JsonUtils.setup(new ObjectMapper());
    }

    @Test
    @SneakyThrows
    public void testRemoveCacheForAllInvalidIndices() {
        val result = dummyClass.methodWithAllInvalidIndices(USER_ID, ENCRYPTION_MODE);

        val expected = getExpectedObject();
        assertEquals(expected, result);

        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(0)).remove(any(), any());
        verify(storedCacheDao, times(0)).removeAll(any());
    }

    @Test
    @SneakyThrows
    public void testRemoveCacheForSomeInvalidIndices() {
        val result = dummyClass.methodWithOneInvalidIndex(USER_ID, ENCRYPTION_MODE);

        val expected = getExpectedObject();
        assertEquals(expected, result);

        val groupingKey = String.join(":", USER_ID, "TEMP");
        val key = String.join(":", ENCRYPTION_MODE.name(), "TEMP");
        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(1)).remove(groupingKey, key);
        verify(storedCacheDao, times(1)).removeAll("TEST");
    }

    @Test
    @SneakyThrows
    public void testRemoveCacheWhenBothFlagsAreTurnedOn() {
        val result = dummyClass.methodWithBothFlagsTurnedOn(USER_ID, ENCRYPTION_MODE);

        val expected = getExpectedObject();
        assertEquals(expected, result);

        val groupingKey = String.join(":", USER_ID, "TEMP");
        val key = String.join(":", ENCRYPTION_MODE.name(), "TEMP");
        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(2)).remove(groupingKey, key);
        verify(storedCacheDao, times(2)).removeAll("TEST");
    }

    @Test
    @SneakyThrows
    public void testRemoveCacheWhenBothFlagsAreTurnedOff() {
        val result = dummyClass.methodWithBothFlagsTurnedOff(USER_ID, ENCRYPTION_MODE);

        val expected = getExpectedObject();
        assertEquals(expected, result);

        val groupingKey = String.join(":", USER_ID, "TEMP");
        val key = String.join(":", ENCRYPTION_MODE.name(), "TEMP");
        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(0)).remove(any(), any());
        verify(storedCacheDao, times(0)).removeAll(any());
    }

    private StoredCache getExpectedObject() {
        return StoredCache.builder()
                .data(BYTES_DATA)
                .encryptionMeta(EncryptionMeta.builder()
                        .encryptionMode(ENCRYPTION_MODE)
                        .build())
                .build();
    }

}
