package ru.dmerkushov.mkcache.backend;

/**
 * Caching backend exception
 */
public class CacheBackendException extends Exception {
    public CacheBackendException() {
    }

    public CacheBackendException(String message) {
        super(message);
    }

    public CacheBackendException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheBackendException(Throwable cause) {
        super(cause);
    }
}
