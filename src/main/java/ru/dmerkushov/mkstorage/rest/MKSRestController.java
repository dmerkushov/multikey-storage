package ru.dmerkushov.mkstorage.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dmerkushov.mkstorage.backend.MultipleFoundInStorageException;
import ru.dmerkushov.mkstorage.backend.NotFoundInStorageException;
import ru.dmerkushov.mkstorage.backend.StorageBackend;
import ru.dmerkushov.mkstorage.backend.StorageBackendException;
import ru.dmerkushov.mkstorage.data.StoredItem;
import ru.dmerkushov.mkstorage.rest.filter.RequestIdFilter;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/v1")
public class MKSRestController {

    private final StorageBackend storageBackend;

    @GetMapping("/")
    public byte[] index() throws Exception {
        log.info("get index /");
        throw new Exception("Cannot continue");
    }

    /**
     * Create or update an item in the storage
     *
     * @param systemName  system name for which to setup the storing
     * @param tagsStr     tags to store the stored item with. Separated by commas
     * @param requestId   generated/or received in {@link RequestIdFilter}
     * @param contentType content type of the item to be stored
     * @param bytes       bytes to be stored
     * @return
     * @throws Exception
     */
    @PostMapping("/{systemName}/{tags}")
    public ResponseEntity<String> post(
            @RequestAttribute("requestId") String requestId,
            @PathVariable("systemName") String systemName,
            @PathVariable("tags") String tagsStr,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody @NonNull byte[] bytes
    ) {
        log.debug(
                "+post(): requestId {}: new data for systemName {}, tagsStr \"{}\": contentType \"{}\", bytes length {} ",
                requestId,
                systemName,
                tagsStr,
                contentType,
                bytes.length
        );

        MediaType mediaType = MediaType.parseMediaType(contentType);

        log.trace("post(): requestId {}: media type parsed: {}", requestId, mediaType);

        Set<String> tags = parseTags(requestId, tagsStr);

        StoredItem newStoredItem = StoredItem.builder()
                .systemName(systemName)
                .tags(tags)
                .mediaType(mediaType)
                .bytes(bytes)
                .storeTimestamp(System.currentTimeMillis())
                .build();

        log.trace("post(): requestId {}: new StoredItem constructed: {}", requestId, newStoredItem);

        StoredItem oldStoredItem = null;
        try {
            oldStoredItem = storageBackend.store(requestId, newStoredItem);
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

    @GetMapping("/{systemName}/{tags}")
    public ResponseEntity<byte[]> get(
            @RequestAttribute("requestId") String requestId,
            @PathVariable("systemName") String systemName,
            @PathVariable("tags") String tagsStr
    ) {
        log.debug(
                "+get(): requestId {}: try to get stored data for systemName {}, tagsStr \"{}\"",
                requestId,
                systemName,
                tagsStr
        );

        Set<String> tags = parseTags(requestId, tagsStr);

        StoredItem storedItem;
        try {
            storedItem = storageBackend.get(requestId, systemName, tags);
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
            log.info("-get(): requestId {}: Unexpected null result from storage backend", requestId);
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

    private Set<String> parseTags(String requestId, String tagsStr) {
        log.trace("+parseTags(): requestId {}: tagsStr {}", requestId, tagsStr);

        Set<String> tags = Arrays.stream(
                tagsStr.split(",")
        ).collect(
                Collectors.toCollection(() -> new HashSet<>())
        );

        log.trace("-parseTags(): requestId {}: tags {}", requestId, tags);
        return tags;
    }
}
