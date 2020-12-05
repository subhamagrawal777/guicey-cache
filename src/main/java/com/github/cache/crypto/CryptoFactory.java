package com.github.cache.crypto;

import com.github.cache.annotations.Encryption;
import com.github.cache.models.EncryptionMeta;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.reflections.Reflections;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class CryptoFactory {
    private static final String BASE_PACKAGE = "com.github.cache";
    private Map<EncryptionMeta, EncryptionService> encryptionMetaEncryptionServiceMap;

    @Inject
    public CryptoFactory(final Injector injector) {
        Reflections reflections = new Reflections(BASE_PACKAGE);
        final Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Encryption.class);

        this.encryptionMetaEncryptionServiceMap = annotatedClasses.stream()
                .filter(EncryptionService.class::isAssignableFrom)
                .collect(Collectors.toMap(
                        annotatedType -> buildEncryptionMeta(annotatedType.getAnnotation(Encryption.class)),
                        annotatedType -> EncryptionService.class.cast(injector.getInstance(annotatedType))));
    }

    public EncryptionService getEncryptionService(EncryptionMeta meta) {
        return encryptionMetaEncryptionServiceMap.get(meta);
    }

    private EncryptionMeta buildEncryptionMeta(Encryption encryption) {
        return EncryptionMeta.builder()
                .encryptionMode(encryption.mode())
                .version(encryption.version())
                .build();
    }
}
