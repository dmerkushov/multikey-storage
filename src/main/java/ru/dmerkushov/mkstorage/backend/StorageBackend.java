package ru.dmerkushov.mkstorage.backend;

import java.util.Set;
import ru.dmerkushov.mkstorage.data.StoredItem;

public interface StorageBackend {
    /**
     * Put an item in the storage.
     * <p>
     * If there is an item in the storage for the given tags, it is removed from the storage, the new one is put in the storage and the old
     * one is returned.
     * <p>
     * If there are several (more than one) entities in the storage for the given tags, the new one is NOT put in the storage and {@link
     * StorageBackendException} is thrown.
     *
     * @param newStoredItem
     * @return null if the item is new for the given storage tags; or the old stored item for the given storage tags if any had existed
     * @throws MultipleFoundInStorageException if there are already several entities in the storage for the given tags
     * @throws StorageBackendException         in case of the storing backend implementation failure
     */
    StoredItem store(String requestId, StoredItem newStoredItem) throws StorageBackendException;

    /**
     * Get an item from the storage for the given tags.
     * <p>
     * If there is no item in the storage for the given tags, an empty Optional is returned.
     * <p>
     * If there are several (more than one) entities in the storage for the given tags, a {@link StorageBackendException} is thrown.
     *
     * @param systemName
     * @param tags
     * @return the found item
     * @throws MultipleFoundInStorageException if there are several entities in the storage for the given tags
     * @throws NotFoundInStorageException      if no item in the storage is found for the given tags
     * @throws StorageBackendException         in case of the storing backend implementation failure
     */
    StoredItem get(String requestId, String systemName, Set<String> tags) throws StorageBackendException;

    /**
     * Count entities found in the storage for the given tags
     *
     * @param tags
     * @return
     * @throws StorageBackendException in case of the storing backend implementation failure
     */
    int countOccurences(String requestId, Set<String> tags) throws StorageBackendException;
}
