package ru.dmerkushov.mkstorage.backend;

/**
 * Multiple items are found in the actual storage
 */
public class MultipleFoundInStorageException extends StorageBackendException {
    public MultipleFoundInStorageException() {
    }

    public MultipleFoundInStorageException(String message) {
        super(message);
    }

    public MultipleFoundInStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleFoundInStorageException(Throwable cause) {
        super(cause);
    }
}
