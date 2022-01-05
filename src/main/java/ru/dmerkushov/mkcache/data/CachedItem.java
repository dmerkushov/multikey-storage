package ru.dmerkushov.mkcache.data;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;

/**
 * An item in the cache
 */
@Data
@Builder
public class CachedItem {

    /**
     * Name of the configured system using the cached item
     */
    private String systemName;

    /**
     * The type of the media stored
     */
    private MediaType mediaType;

    /**
     * Bytes to be stored in the cache
     */
    private byte[] bytes;

    /**
     * Tags to be stored for a cached item
     */
    Set<String> tags;

    /**
     * Timestamp of the entity encaching (as in {@link System#currentTimeMillis()}
     */
    private long encacheTimestamp;
}
