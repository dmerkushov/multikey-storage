package ru.dmerkushov.mkcache.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class CacheMetrics {

    private MeterRegistry meterRegistry;

    /**
     * Total size of all byte arrays contained in the cache, for all system names
     */
    @Getter
    private final AtomicLong cacheTotalBytesSize;

    /**
     * Total quantity of all items contained in the cache, for all system names
     */
    @Getter
    private final AtomicInteger cacheTotalItemQuantity;

    public CacheMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.cacheTotalItemQuantity = meterRegistry.gauge("cache_total_item_quantity", new AtomicInteger(0));
        this.cacheTotalBytesSize = meterRegistry.gauge("cache_total_bytes_size", new AtomicLong(0L));
    }
}
