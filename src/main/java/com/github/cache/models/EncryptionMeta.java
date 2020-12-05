package com.github.cache.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EncryptionMeta {
    private EncryptionMode encryptionMode;
    private Version version;
}
