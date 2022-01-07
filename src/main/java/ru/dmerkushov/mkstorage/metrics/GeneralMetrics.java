package ru.dmerkushov.mkstorage.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralMetrics {

    private final MeterRegistry meterRegistry;

    @Bean
    public Counter errorMultipleFoundCounterGet() {
        return Counter
                .builder("storage_error_multiple_found_counter")
                .tag("method", "get")
                .description("Counter for cases of finding multiple values for the given tag list")
                .register(meterRegistry);
    }

    @Bean
    public Counter errorNotFoundCounterGet() {
        return Counter
                .builder("storage_error_not_found_counter")
                .tag("method", "get")
                .description("Counter for cases of not finding any values for the given tag list")
                .register(meterRegistry);
    }

    @Bean
    public Counter errorMultipleFoundCounterStore() {
        return Counter
                .builder("storage_error_multiple_found_counter")
                .tag("method", "store")
                .description("Counter for cases of finding multiple values for the given tag list")
                .register(meterRegistry);
    }

    @Bean
    public Counter errorNotFoundCounterStore() {
        return Counter
                .builder("storage_error_not_found_counter")
                .tag("method", "store")
                .description("Counter for cases of not finding any values for the given tag list")
                .register(meterRegistry);
    }
}
