package ru.dmerkushov.mkcache.backend;

/**
 * Multiple items are found in the actual cache
 */
public class MultipleFoundInCacheException extends CacheBackendException {
    public MultipleFoundInCacheException() {
    }

    public MultipleFoundInCacheException(String message) {
        super(message);
    }

    public MultipleFoundInCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleFoundInCacheException(Throwable cause) {
        super(cause);
    }
}
