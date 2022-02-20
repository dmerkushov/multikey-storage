package ru.dmerkushov.mkstorage.backend.impl.dummy;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import ru.dmerkushov.mkstorage.backend.StorageBackend;
import ru.dmerkushov.mkstorage.backend.StorageBackendException;
import ru.dmerkushov.mkstorage.data.StoredItem;

@Component("dummy")
@Log4j2
public class DummyStorageBackend implements StorageBackend {

    private final StoredItem storedItem =
            StoredItem.builder()
                    .storeTimestamp(System.currentTimeMillis())
                    .bytes(new byte[0])
                    .mediaType(MediaType.APPLICATION_OCTET_STREAM)
                    .sectionName("")
                    .tags(new HashSet<>())
                    .build();

    @Override
    public StoredItem store(String requestId, StoredItem newStoredItem) {
        log.debug("store(): requestId {}, newStoredItem {}", requestId, newStoredItem);
        return storedItem;
    }

    @Override
    public StoredItem get(String requestId, String sectionName, Set<String> tags) {
        log.debug("get(): requestId {}, sectionName {}, tags {}", requestId, sectionName, tags);
        return storedItem;
    }

    @Override
    public void remove(String requestId, String sectionName, Set<String> tags, boolean forceAll) throws StorageBackendException {
        // NOP
    }

    @Override
    public int countOccurences(String requestId, Set<String> tags) {
        log.debug("countOccurences(): requestId {}, tags {}", requestId, tags);
        return 0;
    }
}
