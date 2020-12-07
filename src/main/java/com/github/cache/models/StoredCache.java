package com.github.cache.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredCache {

    private byte[] data;

    private EncryptionMeta encryptionMeta;

    private long cachedAt;
}
