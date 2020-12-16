package com.github.cache.utils;

import com.github.cache.annotations.Index;
import com.github.cache.models.CacheIndex;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static String buildKey(DocumentContext documentContext, String[] keys) {
        return Arrays.stream(keys)
                .map(key -> {
                    if (key.startsWith("$.")) {
                        val result = documentContext.read(key, String.class);
                        Preconditions.checkNotNull(result);
                        return result;
                    }
                    return key;
                })
                .collect(Collectors.joining(":"));
    }

    public static DocumentContext getDocumentContext(MethodInvocation invocation) {
        val context = Maps.newHashMap();
        for (int i = 0; i < invocation.getMethod().getParameters().length; i++) {
            context.put(invocation.getMethod().getParameters()[i].getName(), invocation.getArguments()[i]);
        }
        return JsonPath.parse(context);
    }

    public static List<CacheIndex> buildCacheIndices(DocumentContext documentContext, Index[] indices) {
        return Arrays.stream(indices)
                .map(index -> CacheIndex.builder()
                        .key(buildKey(documentContext, index.keys()))
                        .groupingKey(buildKey(documentContext, index.groupingKeys()))
                        .build())
                .collect(Collectors.toList());
    }
}
