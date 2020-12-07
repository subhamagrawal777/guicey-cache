package com.github.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cache.storage.StoredCacheDao;
import com.github.cache.utils.JsonUtils;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

public abstract class CacheBundle implements GuiceyBundle {

    public void initialize(GuiceyBootstrap guiceyBootstrap) {
        guiceyBootstrap.modules(new CachingModule(getStoredCacheDao()));
        JsonUtils.setup(objectMapper());
    }

    abstract StoredCacheDao getStoredCacheDao();

    abstract ObjectMapper objectMapper();
}
