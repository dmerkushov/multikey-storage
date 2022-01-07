package ru.dmerkushov.mkstorage.data;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;

/**
 * An item in the storage
 */
@Data
@Builder
public class StoredItem {

    /**
     * Name of the configured system using the stored item
     */
    private String systemName;

    /**
     * The type of the media stored
     */
    private MediaType mediaType;

    /**
     * Bytes to be stored
     */
    private byte[] bytes;

    /**
     * Tags to point to this item in the storage
     */
    Set<String> tags;

    /**
     * Timestamp of the item storing (as in {@link System#currentTimeMillis()}
     */
    private long storeTimestamp;
}
