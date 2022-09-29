package ru.dmerkushov.mkstorage.backend.impl.memmap;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemMapStorageMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * Total size of all byte arrays contained in the storage, for all section names
     */
    @Getter
    private final AtomicLong totalBytesSize = new AtomicLong(0L);

    /**
     * Total quantity of all items contained in the storage, for all section names
     */
    @Getter
    private final AtomicInteger totalItemQuantity = new AtomicInteger(0);

    /**
     * Total quantity of all tags in the storage, for all section names
     */
    @Getter
    private final AtomicInteger totalTagQuantity = new AtomicInteger(0);

    @Bean
    public Gauge totalBytesSizeGauge() {
        return Gauge
                .builder("storage_total_bytes_size", totalBytesSize, AtomicLong::doubleValue)
                .description("Total size of all byte arrays contained in the storage, for all section names")
                .register(meterRegistry);
    }

    @Bean
    public Gauge totalItemQuantityGauge() {
        return Gauge
                .builder("storage_total_item_quantity", totalItemQuantity, AtomicInteger::doubleValue)
                .description("Total quantity of all items contained in the storage, for all section names")
                .register(meterRegistry);
    }

    @Bean
    public Gauge totalTagQuantityGauge() {
        return Gauge
                .builder("storage_total_item_quantity", totalTagQuantity, AtomicInteger::doubleValue)
                .description("Total quantity of all tags in the storage, for all section names")
                .register(meterRegistry);
    }
}
