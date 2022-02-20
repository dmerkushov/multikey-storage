package ru.dmerkushov.mkstorage.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dmerkushov.mkstorage.backend.StorageBackend;

@Configuration
@Log4j2
public class StorageBackendConfig {

    @Bean("configuredStorageBackend")
    public StorageBackend configuredStorageBackend(
            ApplicationContext applicationContext,
            @Value("${mkstorage.storage.engine}") String qualifier
    ) {
        log.info("configuredStorageBackend(): configured storage engine: '{}'", qualifier);
        return (StorageBackend) applicationContext.getBean(qualifier);
    }
}
