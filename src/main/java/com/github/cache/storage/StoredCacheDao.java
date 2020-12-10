package com.github.cache.storage;

import com.github.cache.models.StoredCache;

import java.util.Optional;

public interface StoredCacheDao {
    void save(StoredCache data, String groupingKey, String key, int ttlInSec) throws Exception;

    Optional<StoredCache> get(String groupingKey, String key) throws Exception;

    boolean remove(String groupingKey, String key);

    boolean removeAll(String groupingKey);

}
