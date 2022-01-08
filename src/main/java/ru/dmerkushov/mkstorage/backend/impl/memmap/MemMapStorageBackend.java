package ru.dmerkushov.mkstorage.backend.impl.memmap;

import io.micrometer.core.instrument.Counter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import ru.dmerkushov.mkstorage.backend.MultipleFoundInStorageException;
import ru.dmerkushov.mkstorage.backend.NotFoundInStorageException;
import ru.dmerkushov.mkstorage.backend.StorageBackend;
import ru.dmerkushov.mkstorage.backend.StorageBackendException;
import ru.dmerkushov.mkstorage.data.StoredItem;

@Component
@Log4j2
@RequiredArgsConstructor
public class MemMapStorageBackend implements StorageBackend {

    /**
     * Mapping tags to UUIDs of stored items
     */
    private Map<String, Set<UUID>> tagsToUuids = new HashMap<>();

    /**
     * Mapping UUIDs of stored items to themselves
     */
    private Map<UUID, StoredItem> uuidsToItems = new HashMap<>();

    @Getter
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final MemMapStorageMetrics memMapStorageMetrics;
    private final Counter errorMultipleFoundCounterStore;
    private final Counter errorNotFoundCounterStore;
    private final Counter errorMultipleFoundCounterGet;
    private final Counter errorNotFoundCounterGet;
    private final Counter errorMultipleFoundCounterRemove;
    private final Counter errorNotFoundCounterRemove;

    @Override
    public StoredItem store(String requestId, StoredItem newStoredItem) throws StorageBackendException {
        log.debug("+store(): requestId {}: newStoredItem {}", requestId, newStoredItem);

        lock.writeLock().lock();
        try {
            UUID uuid = null;
            try {
                uuid = getSingleUuidForTags(requestId, newStoredItem.getTags());
            } catch (MultipleFoundInStorageException e) {
                log.error(
                        "-store(): requestId " + requestId +
                                ": multiple existing uuids found when searching for tags of: " + newStoredItem + ". " +
                                "The resulting stored item will become inaccessible. Cancelling the storing action",
                        e
                );

                // Metrics
                errorMultipleFoundCounterStore.increment();

                throw e;
            } catch (NotFoundInStorageException e) {
                log.trace("store(): requestId {}: no existing uuids found for tags of the new stored item", requestId);

                // Metrics
                errorNotFoundCounterStore.increment();

                // Do not rethrow, as this is ok not to find an old item when we are storing a new one ^)
            }
            if (uuid == null) {
                uuid = UUID.randomUUID();

                log.trace("store(): requestId {}: new UUID generated for the new stored item");
            }
            for (String tag : newStoredItem.getTags()) {
                Set<UUID> uuids = tagsToUuids.get(tag);
                if (uuids == null) {
                    uuids = new HashSet<UUID>();
                }
                uuids.add(uuid);
                tagsToUuids.put(tag, uuids);
            }

            StoredItem oldStoredItem = uuidsToItems.put(uuid, newStoredItem);

            // Update the metrics
            if (oldStoredItem == null) {
                memMapStorageMetrics.getTotalBytesSize().addAndGet(newStoredItem.getBytes().length);
                AtomicInteger storageTotalItemQuantity = memMapStorageMetrics.getTotalItemQuantity();
                storageTotalItemQuantity.incrementAndGet();
            } else {
                memMapStorageMetrics.getTotalBytesSize().addAndGet(newStoredItem.getBytes().length - oldStoredItem.getBytes().length);
            }

            log.debug("-store(): requestId {}: old stored item {}", requestId, oldStoredItem);
            return oldStoredItem;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public StoredItem get(String requestId, String systemName, Set<String> tags) throws StorageBackendException {
        log.debug("+get(): requestId {}, systemName {}, tags {}", requestId, systemName, tags);

        lock.readLock().lock();
        try {
            UUID uuid = null;
            try {
                uuid = getSingleUuidForTags(requestId, tags);
            } catch (MultipleFoundInStorageException e) {
                log.error(
                        "-get(): requestId " + requestId +
                                ": multiple existing uuids found when searching for tags: " +
                                tags.stream().collect(Collectors.joining(",")) + ". " +
                                "The stored item being searched for is inaccessible",
                        e
                );

                // Metrics
                errorMultipleFoundCounterGet.increment();

                throw e;
            } catch (NotFoundInStorageException e) {
                log.debug("-get(): requestId {}: no existing uuids found for tags of the new stored item", requestId);

                // Metrics
                errorNotFoundCounterGet.increment();

                throw e;
            }

            // This shouldn't happen, but it's always better to test the null case
            if (uuid == null) {
                log.error("-get(): requestId {}: Unexpected: uuid is null for tags {}", requestId, tags);

                throw new StorageBackendException(
                        "get(): requestId " + requestId + ": " +
                                "Unexpected: uuid is null for tags " +
                                tags.stream().collect(Collectors.joining(","))
                );
            }

            StoredItem item = uuidsToItems.get(uuid);
            if (item != null && item.getSystemName().equals(systemName)) {
                return item;
            } else {
                return null;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public StoredItem remove(String requestId, String systemName, Set<String> tags) throws StorageBackendException {
        log.debug("+remove(): requestId {}, systemName {}, tags {}", requestId, systemName, tags);

        lock.writeLock().lock();
        try {
            UUID uuid = null;
            try {
                uuid = getSingleUuidForTags(requestId, tags);
            } catch (MultipleFoundInStorageException e) {
                log.error(
                        "-remove(): requestId " + requestId +
                                ": multiple existing uuids found when searching for tags: " +
                                tags.stream().collect(Collectors.joining(",")) + ". " +
                                "The stored item being searched for is inaccessible",
                        e
                );

                // Metrics
                errorMultipleFoundCounterRemove.increment();

                throw e;
            } catch (NotFoundInStorageException e) {
                log.trace("-remove(): requestId {}: no existing uuids found for tags of the new stored item", requestId);

                // Metrics
                errorNotFoundCounterRemove.increment();

                throw e;
            }

            // This shouldn't happen, but it's always better to test the null case
            if (uuid == null) {
                log.error("-remove(): requestId {}: Unexpected: uuid is null for tags {}", requestId, tags);

                throw new StorageBackendException(
                        "remove(): requestId " + requestId + ": " +
                                "Unexpected: uuid is null for tags " +
                                tags.stream().collect(Collectors.joining(","))
                );
            }

            for (String tag : tags) {
                Set<UUID> uuids = tagsToUuids.get(tag);
                if (uuids != null) {
                    uuids.remove(uuid);
                    tagsToUuids.put(tag, uuids);
                }
            }

            StoredItem oldStoredItem = uuidsToItems.remove(uuid);

            // Update the metrics
            if (oldStoredItem != null) {
                memMapStorageMetrics.getTotalBytesSize().addAndGet(-oldStoredItem.getBytes().length);
                AtomicInteger storageTotalItemQuantity = memMapStorageMetrics.getTotalItemQuantity();
                storageTotalItemQuantity.decrementAndGet();
            }

            if (oldStoredItem != null && oldStoredItem.getSystemName().equals(systemName)) {
                log.debug("-remove(): requestId {}: old stored item {}", requestId, oldStoredItem);
                return oldStoredItem;
            } else {
                log.debug("-remove(): requestId {}: return null", requestId);
                return null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int countOccurences(String requestId, Set<String> tags) throws StorageBackendException {
        log.debug("+countOccurences(): requestId {}: tags {}", requestId, tags);

        lock.readLock().lock();
        try {
            int occurencesQuantity = getUuidsForTags(requestId, tags).size();
            log.debug("-countOccurences(): requestId {}: {}", requestId, occurencesQuantity);
            return occurencesQuantity;
        } finally {
            lock.readLock().unlock();
        }
    }

    private UUID getSingleUuidForTags(String requestId, Set<String> tags) throws StorageBackendException {
        log.debug("+getSingleUuidForTags(): requestId {}: tags {}", requestId, tags);

        Set<UUID> uuidsForTags = getUuidsForTags(requestId, tags);

        if (uuidsForTags.size() > 1) {
            throw new MultipleFoundInStorageException(
                    "Multiple existing stored items (" + uuidsForTags.size() + ") found for tags list: " +
                            tags.stream().collect(Collectors.joining(","))
            );
        } else if (uuidsForTags.isEmpty()) {
            throw new NotFoundInStorageException(
                    "No items found in storage for tags list: " +
                            tags.stream().collect(Collectors.joining(","))
            );
        }

        return uuidsForTags.iterator().next();
    }

    private Set<UUID> getUuidsForTags(String requestId, Set<String> tags) throws StorageBackendException {
        log.debug("+getUuidsForTags(): requestId {}: tags {}", requestId, tags);

        if (tags.isEmpty()) {
            throw new StorageBackendException("Tags set is empty");
        }

        Iterator<String> tagsIter = tags.iterator();

        String firstTag = tagsIter.next();
        Set<UUID> firstUuids = tagsToUuids.get(firstTag);

        if (firstUuids == null) {
            log.debug(
                    "-getUuidsForTags(): requestId {}: the first tag has no uuids associated, so no uuids will be found. " +
                            "Returning an empty set",
                    requestId
            );
            return new HashSet<>();
        }

        // Create a draft result uuid set and initialize it with the set of uuids of the first tag in the input tag set
        Set<UUID> result = new HashSet<>();
        result.addAll(firstUuids);

        log.trace("getUuidsForTags(): requestId {}: initial draft result {}", requestId, result);

        // Loop over other tags in the input tag set
        // For every tag: for every uuid NOT already contained in the result uuid set, remove it from the result
        while (tagsIter.hasNext()) {
            String currentTag = tagsIter.next();
            Set<UUID> uuidsForCurrentTag = tagsToUuids.get(currentTag);
            if (uuidsForCurrentTag == null) {
                return new HashSet<>();
            }

            Iterator<UUID> resultUuidIterator = result.iterator();
            while (resultUuidIterator.hasNext()) {
                UUID uuid = resultUuidIterator.next();
                if (!uuidsForCurrentTag.contains(uuid)) {
                    log.trace(
                            "getUuidsForTags(): requestId {}: removing uuid {} from the draft result as it is not associated with tag {}",
                            requestId,
                            uuid,
                            currentTag
                    );

                    resultUuidIterator.remove();
                }
            }
            if (result.isEmpty()) {
                log.trace(
                        "getUuidsForTags(): requestId {}: breaking the loop over tags as the draft result is empty already",
                        requestId
                );
                break;
            }
        }

        log.debug("-getUuidsForTags(): requestId {}: found uuids {}", requestId, result);
        return result;
    }
}
