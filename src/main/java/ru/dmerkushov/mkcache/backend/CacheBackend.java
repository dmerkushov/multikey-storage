package ru.dmerkushov.mkcache.backend;

import java.util.Optional;
import java.util.Set;
import ru.dmerkushov.mkcache.data.CachedItem;

public interface CacheBackend {
    /**
     * Put an entity in the cache.
     * <p>
     * If there is an entity in the cache for the given tags, it is removed from the cache, the new one is put in the cache and the old one
     * is returned.
     * <p>
     * If there are several (more than one) entities in the cache for the given tags, the new one is NOT put in the cache and {@link
     * CacheBackendException} is thrown.
     *
     * @param newCachedItem
     * @return null if the entity is new for the given cache tags; or the old cached entity for the given cache tags if any had existed
     * @throws CacheBackendException if there are already several entities in the cache for the given tags, or in case of the caching
     *                               backend implementation failure
     */
    CachedItem encache(CachedItem newCachedItem) throws CacheBackendException;

    /**
     * Get an entity from the cache for the given tags.
     * <p>
     * If there is no entity in the cache for the given tags, an empty Optional is returned.
     * <p>
     * If there are several (more than one) entities in the cache for the given tags, a {@link CacheBackendException} is thrown.
     *
     * @param systemName
     * @param tags
     * @return
     * @throws CacheBackendException if there are several entities in the cache for the given tags, or in case of the caching backend
     *                               implementation failure
     */
    Optional<CachedItem> get(String systemName, Set<String> tags) throws CacheBackendException;

    /**
     * Count entities found in the cache for the given tags
     *
     * @param tags
     * @return
     * @throws CacheBackendException in case of the caching backend implementation failure
     */
    int countOccurences(Set<String> tags) throws CacheBackendException;
}
