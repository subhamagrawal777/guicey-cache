package com.github.cache.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheIndex {
    private String key;
    private String groupingKey;
}
