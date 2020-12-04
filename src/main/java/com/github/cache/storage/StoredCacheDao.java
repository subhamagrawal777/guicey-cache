package com.github.cache.storage;

import com.github.cache.models.StoredCache;

import java.util.Optional;

public interface StoredCacheDao {
    void save(StoredCache data, String key, int ttlInSec);

    Optional<StoredCache> get(String key);

    boolean remove(String key);

}
