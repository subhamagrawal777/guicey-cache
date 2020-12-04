package com.github.cache.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoredCache {

    private byte[] data;

    private EncryptionMeta encryptionMeta;
}
