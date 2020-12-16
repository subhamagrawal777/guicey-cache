package com.github.cache;

import com.github.cache.annotations.RemoveCache;
import com.github.cache.models.CacheIndex;
import com.github.cache.storage.StoredCacheDao;
import com.github.cache.utils.Utils;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.List;

@Slf4j
public class RemoveCacheModule extends AbstractModule {

    @Override
    protected void configure() {
        final RemoveCacheModule.RemoveCacheInterceptor removeCacheInterceptor = new RemoveCacheModule.RemoveCacheInterceptor();
        requestInjection(removeCacheInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(RemoveCache.class), removeCacheInterceptor);
    }

    public class RemoveCacheInterceptor implements MethodInterceptor {

        @Inject
        private StoredCacheDao storedCacheDao;

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            val removeCache = invocation.getMethod().getAnnotation(RemoveCache.class);
            List<CacheIndex> cacheIndices;
            try {
                val documentContext = Utils.getDocumentContext(invocation);
                cacheIndices = Utils.buildCacheIndices(documentContext, removeCache.keySet());
            } catch (Exception e) {
                log.error("Error forming Key. Please check cache param. Method Name: {}", invocation.getMethod().getName(), e);
                return invocation.proceed();
            }

            if (removeCache.before()) {
                removeCache(cacheIndices, invocation.getMethod().getName());
            }

            val object = invocation.proceed();

            if (removeCache.after()) {
                removeCache(cacheIndices, invocation.getMethod().getName());
            }
            return object;
        }

        private void removeCache(List<CacheIndex> cacheIndices, String methodName) {
            cacheIndices.forEach(cacheIndex -> {
                try {
                    if (Strings.isNullOrEmpty(cacheIndex.getKey())) {
                        storedCacheDao.removeAll(cacheIndex.getGroupingKey());
                    } else {
                        storedCacheDao.remove(cacheIndex.getGroupingKey(), cacheIndex.getKey());
                    }
                } catch (Exception e) {
                    log.error("Error while Removing Cache for {} Method Name: {}", cacheIndex, methodName, e);
                }
            });
        }
    }

}
