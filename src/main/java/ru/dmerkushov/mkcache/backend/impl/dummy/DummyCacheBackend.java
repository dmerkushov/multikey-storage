package ru.dmerkushov.mkcache.backend.impl.dummy;

import java.util.Optional;
import java.util.Set;
import ru.dmerkushov.mkcache.backend.CacheBackend;
import ru.dmerkushov.mkcache.backend.CacheBackendException;
import ru.dmerkushov.mkcache.data.CachedItem;

//@Component
public class DummyCacheBackend implements CacheBackend {
    @Override
    public CachedItem encache(CachedItem newCachedItem) throws CacheBackendException {
        return null;
    }

    @Override
    public Optional<CachedItem> get(String systemName, Set<String> tags) throws CacheBackendException {
        return Optional.empty();
    }

    @Override
    public int countOccurences(Set<String> tags) throws CacheBackendException {
        return 0;
    }
}
