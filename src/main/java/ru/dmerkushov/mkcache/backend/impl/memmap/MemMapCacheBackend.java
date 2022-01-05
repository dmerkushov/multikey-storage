package ru.dmerkushov.mkcache.backend.impl.memmap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import ru.dmerkushov.mkcache.backend.CacheBackend;
import ru.dmerkushov.mkcache.backend.CacheBackendException;
import ru.dmerkushov.mkcache.backend.MultipleFoundInCacheException;
import ru.dmerkushov.mkcache.data.CachedItem;
import ru.dmerkushov.mkcache.metrics.CacheMetrics;

@Component
@Log4j2
@RequiredArgsConstructor
public class MemMapCacheBackend implements CacheBackend {

    /**
     * Mapping tags to UUIDs of stored items
     */
    private Map<String, Set<UUID>> tagsToUuids = new HashMap<>();

    /**
     * Mapping UUIDs of stored items to themselves
     */
    private Map<UUID, CachedItem> uuidsToItems = new HashMap<>();

    @Getter
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheMetrics cacheMetrics;

    @Override
    public CachedItem encache(CachedItem newCachedItem) throws CacheBackendException {

        lock.writeLock().lock();
        try {
            UUID uuid = getSingleUuidForTags(newCachedItem.getTags());
            if (uuid == null) {
                uuid = UUID.randomUUID();
            }
            for (String tag : newCachedItem.getTags()) {
                Set<UUID> uuids = tagsToUuids.get(tag);
                if (uuids == null) {
                    uuids = new HashSet<UUID>();
                }
                uuids.add(uuid);
                tagsToUuids.put(tag, uuids);
            }

            CachedItem oldItem = uuidsToItems.put(uuid, newCachedItem);

            // Update the metrics
            AtomicLong cacheTotalBytesSize = cacheMetrics.getCacheTotalBytesSize();
            if (oldItem == null) {
                cacheTotalBytesSize.addAndGet(newCachedItem.getBytes().length);
                AtomicInteger cacheTotalItemQuantity = cacheMetrics.getCacheTotalItemQuantity();
                cacheTotalItemQuantity.incrementAndGet();
            } else {
                cacheTotalBytesSize.addAndGet(newCachedItem.getBytes().length - oldItem.getBytes().length);
            }

            return oldItem;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<CachedItem> get(String systemName, Set<String> tags) throws CacheBackendException {
        lock.readLock().lock();
        try {
            UUID uuid = getSingleUuidForTags(tags);

            if (uuid == null) {
                return Optional.empty();
            }

            CachedItem item = uuidsToItems.get(uuid);
            if (item != null && item.getSystemName().equals(systemName)) {
                return Optional.of(item);
            } else {
                return Optional.empty();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int countOccurences(Set<String> tags) throws CacheBackendException {
        lock.readLock().lock();
        try {
            return getUuidsForTags(tags).size();
        } finally {
            lock.readLock().unlock();
        }
    }

    private UUID getSingleUuidForTags(Set<String> tags) throws CacheBackendException {
        Set<UUID> uuidsForTags = getUuidsForTags(tags);

        if (uuidsForTags.size() > 1) {
            throw new MultipleFoundInCacheException(
                    "Multiple existing cached items (" + uuidsForTags.size() + ") found for tags list: " +
                            tags.stream().collect(Collectors.joining(","))
            );
        }
        if (uuidsForTags.isEmpty()) {
            return null;
        }

        return uuidsForTags.iterator().next();
    }

    private Set<UUID> getUuidsForTags(Set<String> tags) throws CacheBackendException {
        if (tags.isEmpty()) {
            throw new CacheBackendException("Tags set is empty");
        }

        Set<UUID> uuidsFound = new HashSet<>();
        Iterator<String> tagsIter = tags.iterator();

        String firstTag = tagsIter.next();
        Set<UUID> firstUuids = tagsToUuids.get(firstTag);

        if (firstUuids == null) {
            return new HashSet<>();
        }

        uuidsFound.addAll(firstUuids);

        while (tagsIter.hasNext()) {
            String tag = tagsIter.next();
            Set<UUID> uuidsForTag = tagsToUuids.get(tag);
            if (uuidsForTag == null) {
                return new HashSet<>();
            }

            for (UUID uuid : uuidsFound) {
                if (!uuidsForTag.contains(uuid)) {
                    uuidsFound.remove(uuid);
                }
                if (uuidsFound.isEmpty()) {
                    return new HashSet<>();
                }
            }
        }

        return uuidsFound;
    }
}
