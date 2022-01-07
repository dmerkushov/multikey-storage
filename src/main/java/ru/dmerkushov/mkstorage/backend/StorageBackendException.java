package ru.dmerkushov.mkstorage.backend;

/**
 * Exception of the storage backend
 */
public class StorageBackendException extends Exception {
    public StorageBackendException() {
    }

    public StorageBackendException(String message) {
        super(message);
    }

    public StorageBackendException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageBackendException(Throwable cause) {
        super(cause);
    }
}
