package com.github.cache;

import com.github.cache.storage.StoredCacheDao;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

public abstract class CacheBundle implements GuiceyBundle {

    public void initialize(GuiceyBootstrap guiceyBootstrap) {
        guiceyBootstrap.modules(new CachingModule(this::getStoredCacheDao));
    }

    public abstract StoredCacheDao getStoredCacheDao();
}
