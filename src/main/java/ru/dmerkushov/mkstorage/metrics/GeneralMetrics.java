package ru.dmerkushov.mkstorage.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GeneralMetrics {

    private final MeterRegistry meterRegistry;

    public static final String NAME_ERROR_MULTIPLE_FOUND_COUNTER = "storage_error_multiple_found_counter";
    public static final String NAME_ERROR_NOT_FOUND_COUNTER = "storage_error_not_found_counter";

    public static final String OPER_GET = "get";
    public static final String OPER_STORE = "store";
    public static final String OPER_REMOVE = "remove";

    public final Map<String, String> counterDescriptions;

    public GeneralMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        Map<String, String> counterDescriptionsDraft = new HashMap<>();
        counterDescriptionsDraft.put(NAME_ERROR_MULTIPLE_FOUND_COUNTER, "Cases of finding multiple values for the given tag list");
        counterDescriptionsDraft.put(NAME_ERROR_NOT_FOUND_COUNTER, "Cases of not finding any values for the given tag list");

        this.counterDescriptions = Collections.unmodifiableMap(counterDescriptionsDraft);
    }

    @Bean
    public Counter errorMultipleFoundCounterGet() {
        return buildCounter(NAME_ERROR_MULTIPLE_FOUND_COUNTER, OPER_GET);
    }

    @Bean
    public Counter errorNotFoundCounterGet() {
        return buildCounter(NAME_ERROR_NOT_FOUND_COUNTER, OPER_GET);
    }

    @Bean
    public Counter errorMultipleFoundCounterStore() {
        return buildCounter(NAME_ERROR_MULTIPLE_FOUND_COUNTER, OPER_STORE);
    }

    @Bean
    public Counter errorNotFoundCounterStore() {
        return buildCounter(NAME_ERROR_NOT_FOUND_COUNTER, OPER_STORE);
    }

    @Bean
    public Counter errorMultipleFoundCounterRemove() {
        return buildCounter(NAME_ERROR_MULTIPLE_FOUND_COUNTER, OPER_REMOVE);
    }

    @Bean
    public Counter errorNotFoundCounterRemove() {
        return buildCounter(NAME_ERROR_NOT_FOUND_COUNTER, OPER_REMOVE);
    }

    private Counter buildCounter(
            String name,
            String operationTag
    ) {
        return Counter
                .builder(name)
                .tag("operation", operationTag)
                .description(counterDescriptions.get(name))
                .register(meterRegistry);
    }
}
