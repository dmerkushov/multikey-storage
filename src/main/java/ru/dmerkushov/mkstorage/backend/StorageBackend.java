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
     * If there are several (more than one) items in the storage for the given tags, the new one is NOT put in the storage and {@link
     * MultipleFoundInStorageException} is thrown.
     * <p>
     * It is recommended that the tags group set for an item don't interfere with other tag groups, currently existing or
     *
     * @param newStoredItem
     * @return null if the item is new for the given storage tags; or the old stored item for the given storage tags if any had existed
     * @throws MultipleFoundInStorageException if there are already several entities in the storage for the given tags
     * @throws StorageBackendException         in case of the storing backend implementation failure
     */
    StoredItem store(String requestId, StoredItem newStoredItem) throws StorageBackendException;

    /**
     * Get an item from the storage associated with the given tags. The given tags group must point to a single item in the storage.
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
     * Delete an item in the storage for the given tags. The given tags group must point to a single item in the storage.
     *
     * @param requestId
     * @param systemName
     * @param tags
     * @return the deleted item
     * @throws MultipleFoundInStorageException if there are several entities in the storage for the given tags
     * @throws NotFoundInStorageException      if no item in the storage is found for the given tags
     * @throws StorageBackendException         in case of the storing backend implementation failure
     */
    StoredItem remove(String requestId, String systemName, Set<String> tags) throws StorageBackendException;

    /**
     * Count entities found in the storage for the given tags
     * <p>
     * If there is an item in the storage for the given tags, it is removed from the storage and returned
     *
     * @param tags
     * @return
     * @throws MultipleFoundInStorageException if there are several entities in the storage for the given tags
     * @throws NotFoundInStorageException      if no item in the storage is found for the given tags
     * @throws StorageBackendException         in case of the storing backend implementation failure
     */
    int countOccurences(String requestId, Set<String> tags) throws StorageBackendException;
}
