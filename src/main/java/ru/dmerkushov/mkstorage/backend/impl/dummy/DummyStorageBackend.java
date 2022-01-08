package ru.dmerkushov.mkstorage.backend.impl.dummy;

import java.util.Set;
import lombok.extern.log4j.Log4j2;
import ru.dmerkushov.mkstorage.backend.StorageBackend;
import ru.dmerkushov.mkstorage.backend.StorageBackendException;
import ru.dmerkushov.mkstorage.data.StoredItem;

//@Component
@Log4j2
public class DummyStorageBackend implements StorageBackend {
    @Override
    public StoredItem store(String requestId, StoredItem newStoredItem) {
        log.debug("store(): requestId {}, storedItem {}", requestId, newStoredItem);
        return null;
    }

    @Override
    public StoredItem get(String requestId, String systemName, Set<String> tags) {
        log.debug("get(): requestId {}, systemName {}, tags {}", requestId, systemName, tags);
        return null;
    }

    @Override
    public StoredItem remove(String requestId, String systemName, Set<String> tags) throws StorageBackendException {
        return null;
    }

    @Override
    public int countOccurences(String requestId, Set<String> tags) {
        log.debug("countOccurences(): requestId {}, tags {}", requestId, tags);
        return 0;
    }
}
