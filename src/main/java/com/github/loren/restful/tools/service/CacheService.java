package com.github.loren.restful.tools.service;

import com.intellij.openapi.project.Project;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
@SuppressWarnings("UnusedReturnValue")
public interface CacheService<K, V> {

    /**
     * instance
     *
     * @param project auto
     * @return CacheService
     */
    static CacheService<?, ?> getInstance(@NotNull Project project) {
        return project.getService(CacheService.class);
    }

    /**
     * clearCache3333333
     *
     * @param name key
     * @return bool
     */
    boolean clearCache(@NotNull K name);

    /**
     * clear all cache
     *
     * @return clear cache num
     */
    long clearAll();

    /**
     * hasCache
     *
     * @param name key
     * @return bool
     */
    boolean hasCache(@NotNull K name);

    /**
     * set cache and get old cache that if has cache
     *
     * @param name  name
     * @param cache cache
     * @return old Cache
     */
    @Nullable
    V setCache(@NotNull K name, @NotNull V cache);

    /**
     * get cache
     *
     * @param name key
     * @return cache
     */
    @Nullable
    V getCache(@NotNull K name);

    /**
     * get cache
     *
     * @param name         key
     * @param defaultValue defaultValue
     * @return cache
     */
    @NotNull
    V getCache(@NotNull K name, @NotNull V defaultValue);

    /**
     * get caches
     *
     * @return map
     */
    @NotNull
    Map<K, V> getCaches();
}
