package com.github.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cache.crypto.CryptoFactory;
import com.github.cache.crypto.EncryptionService;
import com.github.cache.models.EncryptionMeta;
import com.github.cache.models.EncryptionMode;
import com.github.cache.models.StoredCache;
import com.github.cache.models.Version;
import com.github.cache.storage.StoredCacheDao;
import com.github.cache.utils.CompressionUtils;
import com.github.cache.utils.JsonUtils;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class CachingModuleTest {

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
                new CachingModule(storedCacheDao),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(EncryptionService.class).toInstance(encryptionService);
                        bind(CryptoFactory.class).toInstance(cryptoFactory);
                    }
                });

        dummyClass = injector.getInstance(DummyClass.class);

        when(encryptionService.encrypt(USER_ID_IN_BYTES)).thenReturn(BYTES_DATA);

        JsonUtils.setup(new ObjectMapper());
    }

    @Test
    @SneakyThrows
    public void testInterceptorForNonAnnotatedMethod() {

        val result = dummyClass.storedCacheMethodWithoutAnnotation(USER_ID, ENCRYPTION_MODE);

        val expected = getExpectedObject();

        assertEquals(expected, result);
        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(0)).get(any(), any());
        verify(storedCacheDao, times(0)).save(any(), any(), any(), anyInt());
    }

    @Test
    @SneakyThrows
    public void testInterceptorForAnnotatedMethod() {
        val key = String.join(":", USER_ID, ENCRYPTION_MODE.name(), "method");
        when(storedCacheDao.get(DEFAULT_GROUPING_KEY, key)).thenReturn(Optional.empty());

        val expected = getExpectedObject();

        val result = dummyClass.storedCacheMethod(USER_ID, ENCRYPTION_MODE);

        assertEquals(expected, result);

        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(1)).get(DEFAULT_GROUPING_KEY, key);
        verify(storedCacheDao, times(1)).save(any(StoredCache.class), eq(DEFAULT_GROUPING_KEY), eq(key), eq(600));
    }

    @Test
    @SneakyThrows
    public void testInterceptorForAnnotatedMethodWhenDataWasInCache() {
        val key = String.join(":", USER_ID, ENCRYPTION_MODE.name(), "method");
        val expected = getExpectedObject();
        val storedCache = StoredCache.builder()
                .data(CompressionUtils.encode(expected))
                .cachedAt(122143)
                .build();

        when(storedCacheDao.get(DEFAULT_GROUPING_KEY, key)).thenReturn(Optional.of(storedCache));

        val result = dummyClass.storedCacheMethod(USER_ID, ENCRYPTION_MODE);

        assertEquals(expected, result);

        verify(encryptionService, times(0)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(1)).get(DEFAULT_GROUPING_KEY, key);
        verify(storedCacheDao, times(0)).save(any(), any(), any(), anyInt());
    }

    @Test
    @SneakyThrows
    public void testInterceptorWhenUnableToRetrieveData() {
        val key = String.join(":", USER_ID, ENCRYPTION_MODE.name(), "method");
        when(storedCacheDao.get(DEFAULT_GROUPING_KEY, key)).thenThrow(new IllegalArgumentException());

        val resultWhenInvokedOnce = dummyClass.storedCacheMethod(USER_ID, ENCRYPTION_MODE);
        val resultWhenInvokedTwice = dummyClass.storedCacheMethod(USER_ID, ENCRYPTION_MODE);

        val expected = getExpectedObject();

        assertEquals(expected, resultWhenInvokedOnce);
        assertEquals(expected, resultWhenInvokedTwice);

        verify(encryptionService, times(2)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(2)).get(DEFAULT_GROUPING_KEY, key);
        verify(storedCacheDao, times(0)).save(any(), any(), any(), anyInt());
    }

    @Test
    @SneakyThrows
    public void testInterceptorWhenUnableToSaveData() {
        val key = String.join(":", USER_ID, ENCRYPTION_MODE.name(), "method");
        when(storedCacheDao.get(DEFAULT_GROUPING_KEY, key)).thenReturn(Optional.empty());

        doThrow(new IllegalArgumentException()).when(storedCacheDao).save(any(StoredCache.class), eq(DEFAULT_GROUPING_KEY), eq(key), eq(600));

        val resultWhenInvokedOnce = dummyClass.storedCacheMethod(USER_ID, ENCRYPTION_MODE);
        val resultWhenInvokedTwice = dummyClass.storedCacheMethod(USER_ID, ENCRYPTION_MODE);

        val expected = getExpectedObject();
        assertEquals(expected, resultWhenInvokedOnce);
        assertEquals(expected, resultWhenInvokedTwice);

        verify(encryptionService, times(2)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(2)).get(DEFAULT_GROUPING_KEY, key);
        verify(storedCacheDao, times(2)).save(any(StoredCache.class), eq(DEFAULT_GROUPING_KEY), eq(key), eq(600));
    }

    @Test
    @SneakyThrows
    public void testInterceptorForParameterizedMethod() {

        val key = String.join(":", USER_ID);
        val expected = ImmutableList.of(getExpectedObject(), getExpectedObject());
        val storedCache = StoredCache.builder()
                .data(CompressionUtils.encode(expected))
                .cachedAt(122143)
                .build();

        when(storedCacheDao.get(DEFAULT_GROUPING_KEY, key))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(storedCache));


        val resultWhenInvokedOnce = dummyClass.storedCacheListMethod(USER_ID, ENCRYPTION_MODE);
        val resultWhenInvokedTwice = dummyClass.storedCacheListMethod(USER_ID, ENCRYPTION_MODE);

        assertEquals(expected, resultWhenInvokedOnce);

        assertEquals(expected, resultWhenInvokedTwice);

        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(2)).get(DEFAULT_GROUPING_KEY, key);
        verify(storedCacheDao, times(1)).save(any(StoredCache.class), eq(DEFAULT_GROUPING_KEY), eq(key), eq(600));
    }

    @Test
    @SneakyThrows
    public void testInterceptorForAnnotatedMethodWithEncryption() {
        val key = String.join(":", USER_ID, ENCRYPTION_MODE.name());
        when(storedCacheDao.get(DEFAULT_GROUPING_KEY, key)).thenReturn(Optional.empty());

        val expected = getExpectedObject();

        when(cryptoFactory.getDefaultEncryptionService()).thenReturn(defaultEncryptionService);
        when(defaultEncryptionService.encrypt(CompressionUtils.encode(expected))).thenReturn(CompressionUtils.encode(expected));

        val result = dummyClass.storedCacheMethodWithEncryption(USER_ID, ENCRYPTION_MODE);

        assertEquals(expected, result);

        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(defaultEncryptionService, times(1)).encrypt(CompressionUtils.encode(expected));
        verify(storedCacheDao, times(1)).get(DEFAULT_GROUPING_KEY, key);
        verify(storedCacheDao, times(1)).save(any(StoredCache.class), eq(DEFAULT_GROUPING_KEY), eq(key), eq(100));
    }

    @Test
    @SneakyThrows
    public void testInterceptorForAnnotatedMethodWithEncryptionWhenCacheIsPresent() {
        val key = String.join(":", USER_ID, ENCRYPTION_MODE.name());

        val expected = getExpectedObject();
        val encryptionMeta = EncryptionMeta.builder()
                .encryptionMode(EncryptionMode.AES)
                .version(Version.V1)
                .build();
        val storedCache = StoredCache.builder()
                .data(CompressionUtils.encode(expected))
                .cachedAt(122143)
                .encryptionMeta(encryptionMeta)
                .build();
        when(storedCacheDao.get(DEFAULT_GROUPING_KEY, key)).thenReturn(Optional.of(storedCache));

        when(cryptoFactory.getEncryptionService(encryptionMeta)).thenReturn(defaultEncryptionService);
        when(defaultEncryptionService.decrypt(storedCache.getData())).thenReturn(storedCache.getData());

        val result = dummyClass.storedCacheMethodWithEncryption(USER_ID, ENCRYPTION_MODE);


        assertEquals(expected, result);

        verify(encryptionService, times(0)).encrypt(USER_ID_IN_BYTES);
        verify(defaultEncryptionService, times(1)).decrypt(CompressionUtils.encode(expected));
        verify(storedCacheDao, times(1)).get(DEFAULT_GROUPING_KEY, key);
        verify(storedCacheDao, times(0)).save(any(StoredCache.class), eq(DEFAULT_GROUPING_KEY), eq(key), eq(100));
    }

    @Test
    @SneakyThrows
    public void testInterceptorForAnnotatedMethodWhenExpiredDataIsInCache() {
        val key = String.join(":", USER_ID, ENCRYPTION_MODE.name(), "method");
        val expected = getExpectedObject();
        val storedCache = StoredCache.builder()
                .data(CompressionUtils.encode(expected))
                .cachedAt(122143)
                .build();

        when(storedCacheDao.get(DEFAULT_GROUPING_KEY, key)).thenReturn(Optional.of(storedCache));

        val result = dummyClass.storedCacheMethodWithStructureChange(USER_ID, ENCRYPTION_MODE);

        assertEquals(expected, result);

        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(1)).get(DEFAULT_GROUPING_KEY, key);
        verify(storedCacheDao, times(1)).save(any(StoredCache.class), eq(DEFAULT_GROUPING_KEY), eq(key), eq(150));
    }

    @Test
    @SneakyThrows
    public void testInterceptorWhenUnableToFormKey() {
        val resultWhenInvokedOnce = dummyClass.methodWithInvalidKey(USER_ID, ENCRYPTION_MODE);
        val resultWhenInvokedTwice = dummyClass.methodWithInvalidKey(USER_ID, ENCRYPTION_MODE);

        val expected = getExpectedObject();

        assertEquals(expected, resultWhenInvokedOnce);
        assertEquals(expected, resultWhenInvokedTwice);

        verify(encryptionService, times(2)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(0)).get(any(), any());
        verify(storedCacheDao, times(0)).save(any(), any(), any(), anyInt());
    }

    @Test
    @SneakyThrows
    public void testInterceptorForVoidMethod() {
        dummyClass.voidMethod(USER_ID);

        verify(encryptionService, times(1)).encrypt(USER_ID_IN_BYTES);
        verify(storedCacheDao, times(0)).get(any(), any());
        verify(storedCacheDao, times(0)).save(any(), any(), any(), anyInt());
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