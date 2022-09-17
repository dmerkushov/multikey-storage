package ru.dmerkushov.mkstorage.backend.impl;

import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import ru.dmerkushov.mkstorage.backend.StorageBackend;

@RequiredArgsConstructor
public abstract class RealStorageBackend implements StorageBackend {
    protected final RealStorageMetrics realStorageMetrics;
    protected final Counter errorMultipleFoundCounterStore;
    protected final Counter errorNotFoundCounterStore;
    protected final Counter errorMultipleFoundCounterGet;
    protected final Counter errorNotFoundCounterGet;
    protected final Counter errorMultipleFoundCounterRemove;
    protected final Counter errorNotFoundCounterRemove;
}
