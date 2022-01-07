package ru.dmerkushov.mkstorage.backend;

/**
 * Multiple items are found in the actual storage
 */
public class NotFoundInStorageException extends StorageBackendException {
    public NotFoundInStorageException() {
    }

    public NotFoundInStorageException(String message) {
        super(message);
    }

    public NotFoundInStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundInStorageException(Throwable cause) {
        super(cause);
    }
}
