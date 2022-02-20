package ru.dmerkushov.mkstorage.rest;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dmerkushov.mkstorage.backend.MultipleFoundInStorageException;
import ru.dmerkushov.mkstorage.backend.NotFoundInStorageException;
import ru.dmerkushov.mkstorage.backend.StorageBackend;
import ru.dmerkushov.mkstorage.backend.StorageBackendException;
import ru.dmerkushov.mkstorage.data.StoredItem;
import ru.dmerkushov.mkstorage.rest.filter.RequestIdFilter;

@RestController
@Log4j2
@RequestMapping("/v1")
public class MKSRestControllerV1 {

    @Autowired
    @Qualifier("configuredStorageBackend")
    private StorageBackend configuredStorageBackend;

    @GetMapping("/")
    public ResponseEntity<String> index() {
        log.info("get index / - shall return NOT FOUND");
        return ResponseEntity.notFound().build();
    }

    /**
     * Create or update an item in the storage
     *
     * @param requestId   ID of the request to the REST endpoint. See {@link RequestIdFilter}
     * @param sectionName section name for which to setup the storing
     * @param tagsStr     tags to store the stored item with. Separated by commas
     * @param requestId   generated/or received in {@link RequestIdFilter}
     * @param contentType content type of the item to be stored
     * @param bytes       bytes to be stored
     * @return
     */
    @PostMapping("/{sectionName}/{tags}")
    public ResponseEntity<String> post(
            @RequestAttribute("requestId") String requestId,
            @PathVariable("sectionName") String sectionName,
            @PathVariable("tags") String tagsStr,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody @NonNull byte[] bytes
    ) {
        log.debug(
                "+post(): requestId {}: new data for sectionName {}, tagsStr \"{}\": contentType \"{}\", bytes length {} ",
                requestId,
                sectionName,
                tagsStr,
                contentType,
                bytes.length
        );

        MediaType mediaType = MediaType.parseMediaType(contentType);

        log.trace("post(): requestId {}: media type parsed: {}", requestId, mediaType);

        Set<String> tags = parseTags(requestId, tagsStr);

        StoredItem newStoredItem = StoredItem.builder()
                .sectionName(sectionName)
                .tags(tags)
                .mediaType(mediaType)
                .bytes(bytes)
                .storeTimestamp(System.currentTimeMillis())
                .build();

        log.trace("post(): requestId {}: new StoredItem constructed: {}", requestId, newStoredItem);

        StoredItem oldStoredItem = null;
        try {
            oldStoredItem = configuredStorageBackend.store(requestId, newStoredItem);
        } catch (MultipleFoundInStorageException e) {
            log.warn("-post(): requestId " + requestId + ": Multiple values found in the storage for the given tags", e);

            return ResponseEntity.status(HttpStatus.CONFLICT).build();  // status 409
        } catch (StorageBackendException e) {
            log.error("-post(): requestId " + requestId + ": Exception when storing", e);
            return ResponseEntity.internalServerError().build();
        }

        if (oldStoredItem != null) {
            log.trace("post(): requestId {}: removed old item: {}", requestId, oldStoredItem);
        } else {
            log.trace("post(): requestId {}: no old item had existed, created a new one", requestId);
        }

        log.debug("-post(): requestId {}: OK", requestId);
        return ResponseEntity.ok("OK");
    }

    /**
     * Get an item from the storage
     *
     * @param requestId   ID of the request to the REST endpoint. See {@link RequestIdFilter}
     * @param sectionName section name for which to setup the storing
     * @param tagsStr     tags to search the stored item with. Separated by commas
     * @return
     */
    @GetMapping("/{sectionName}/{tags}")
    public ResponseEntity<byte[]> get(
            @RequestAttribute("requestId") String requestId,
            @PathVariable("sectionName") String sectionName,
            @PathVariable("tags") String tagsStr
    ) {
        log.debug(
                "+get(): requestId {}: try to get stored data for sectionName {}, tagsStr \"{}\"",
                requestId,
                sectionName,
                tagsStr
        );

        Set<String> tags = parseTags(requestId, tagsStr);

        StoredItem storedItem;
        try {
            storedItem = configuredStorageBackend.get(requestId, sectionName, tags);
        } catch (MultipleFoundInStorageException e) {
            log.warn("-get(): requestId " + requestId + ": Multiple values found in the storage for the given tags", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();  // status 409
        } catch (NotFoundInStorageException e) {
            log.warn("-get(): requestId " + requestId + ": No values found in the storage for the given tags", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // status 404
        } catch (StorageBackendException e) {
            log.error("-get(): requestId " + requestId + ": Unexpected storage backend error", e);
            return ResponseEntity.internalServerError().build();
        }
        if (storedItem == null) {
            log.error("-get(): requestId {}: Unexpected null result from storage backend", requestId);
            return ResponseEntity.internalServerError().build();
        }

        ResponseEntity<byte[]> responseEntity = ResponseEntity
                .status(HttpStatus.OK)
                .contentType(storedItem.getMediaType())
                .body(storedItem.getBytes());
        log.debug(
                "-get(): requestId {}: OK with content type {}, bytes length {}",
                requestId,
                storedItem.getMediaType(),
                storedItem.getBytes().length
        );
        return responseEntity;
    }

    /**
     * Delete an item in the storage
     *
     * @param requestId   ID of the request to the REST endpoint. See {@link RequestIdFilter}
     * @param sectionName section name for which to setup the storing
     * @param tagsStr     tags to search the stored item with. Separated by commas
     * @return
     */
    @DeleteMapping("/{sectionName}/{tags}")
    public ResponseEntity<String> delete(
            @RequestAttribute("requestId") String requestId,
            @PathVariable("sectionName") String sectionName,
            @PathVariable("tags") String tagsStr,
            @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force
    ) {
        log.debug(
                "+delete(): requestId {}: try to get stored data for sectionName {}, tagsStr \"{}\"",
                requestId,
                sectionName,
                tagsStr
        );

        Set<String> tags = parseTags(requestId, tagsStr);

        try {
            configuredStorageBackend.remove(requestId, sectionName, tags, Optional.ofNullable(force).orElse(false));
        } catch (MultipleFoundInStorageException e) {
            log.warn("-delete(): requestId " + requestId + ": Multiple values found in the storage for the given tags", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();  // status 409
        } catch (NotFoundInStorageException e) {
            log.warn("-delete(): requestId " + requestId + ": No values found in the storage for the given tags", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // status 404
        } catch (StorageBackendException e) {
            log.error("-delete(): requestId " + requestId + ": Unexpected storage backend error", e);
            return ResponseEntity.internalServerError().build();
        }

        log.debug(
                "-delete(): requestId {}: OK",
                requestId
        );
        return ResponseEntity.ok("OK");
    }

    private Set<String> parseTags(String requestId, String tagsStr) {
        log.trace("+parseTags(): requestId {}: tagsStr {}", requestId, tagsStr);

        Set<String> tags = Arrays.stream(
                        Optional.ofNullable(tagsStr).orElse("").split(",")
                )
                .map(String::trim)
                .sorted()
                .collect(
                        Collectors.toCollection(LinkedHashSet::new)
                );

        log.trace("-parseTags(): requestId {}: tags {}", requestId, tags);
        return tags;
    }
}
