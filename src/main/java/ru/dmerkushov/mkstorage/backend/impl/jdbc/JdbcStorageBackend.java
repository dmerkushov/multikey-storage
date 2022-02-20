package ru.dmerkushov.mkstorage.backend.impl.jdbc;

import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import ru.dmerkushov.mkstorage.backend.StorageBackend;
import ru.dmerkushov.mkstorage.backend.StorageBackendException;
import ru.dmerkushov.mkstorage.data.StoredItem;

@Component("jdbc")
@Log4j2
public class JdbcStorageBackend implements StorageBackend {
    @Override
    public StoredItem store(String requestId, StoredItem newStoredItem) {
        log.debug("store(): requestId {}, storedItem {}", requestId, newStoredItem);
        // Not yet implemented
        return null;
    }

    @Override
    public StoredItem get(String requestId, String sectionName, Set<String> tags) {
        log.debug("get(): requestId {}, sectionName {}, tags {}", requestId, sectionName, tags);
        // Not yet implemented
        return null;
    }

    @Override
    public void remove(String requestId, String sectionName, Set<String> tags, boolean forceAll) throws StorageBackendException {
        // Not yet implemented
    }

    @Override
    public int countOccurences(String requestId, Set<String> tags) {
        log.debug("countOccurences(): requestId {}, tags {}", requestId, tags);
        // Not yet implemented
        return 0;
    }
}
