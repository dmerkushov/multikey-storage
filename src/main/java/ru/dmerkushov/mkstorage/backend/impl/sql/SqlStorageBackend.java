package ru.dmerkushov.mkstorage.backend.impl.sql;

import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import ru.dmerkushov.mkstorage.backend.StorageBackend;
import ru.dmerkushov.mkstorage.backend.StorageBackendException;
import ru.dmerkushov.mkstorage.data.StoredItem;

@Component("sql")
@Log4j2
public class SqlStorageBackend implements StorageBackend {
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

    String createDbSql() {
        return "" +
                "CREATE TABLE IF NOT EXISTS mks_tag_tags(" +
                "   tag_id BIGINT NOT NULL PRIMARY KEY," +
                "   tag_txt VARCHAR(100)" +
                ");" +
                "CREATE INDEX mks_tag_txt_idx ON mks_tag_tags(tag_txt);" +
                "CREATE TABLE IF NOT EXISTS mks_obj_objects(" +
                "   obj_id BIGINT NOT NULL PRIMARY KEY," +
                "   obj_mime VARCHAR(200)," +
                "   obj_keepuntil TIMESTAMP WITH TIME ZONE," +
                "   obj_content BLOB" +
                ");" +
                "CREATE TABLE IF NOT EXISTS mks_lnk_links_obj_tag(" +
                "   lnk_id BIGINT NOT NULL PRIMARY KEY," +
                "   lnk_tag_id BIGINT," +
                "   lnk_obj_id BIGINT," +
                "   CONSTRAINT(lnk_tag_fk) FOREIGN KEY(lnk_tag_id) REFERENCES mks_tag_tags(tag_id)," +
                "   CONSTRAINT(lnk_obj_fk) FOREIGN KEY(lnk_obj_id) REFERENCES mks_obj_objectss(obj_id)," +
                ");" +
                "CREATE INDEX mks_lnk_tag_idx ON mks_lnk_links_obj_tag(lnk_tag_id);" +
                "";
    }
}
