package ru.dmerkushov.mkcache.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RestController;
import ru.dmerkushov.mkcache.backend.CacheBackend;
import ru.dmerkushov.mkcache.backend.CacheBackendException;
import ru.dmerkushov.mkcache.data.CachedItem;
import ru.dmerkushov.mkcache.rest.filter.RequestIdFilter;

@RestController
@Log4j2
@RequiredArgsConstructor
public class MKCRestController {

    private final CacheBackend cacheBackend;

    @GetMapping("/")
    public byte[] index() throws Exception {
        log.info("get index /");
        throw new Exception("Cannot continue");
    }

    /**
     * Create or update an item in the cache
     *
     * @param systemName  system name for which to setup the caching
     * @param tagsStr     tags to store the cached entity with. Separated by commas
     * @param requestId   generated/or received in {@link RequestIdFilter}
     * @param contentType content type of the entity to be cached
     * @param bytes       bytes to be cached
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
                "+post(): requestId {}: new cached data for systemName {}, tagsStr \"{}\": contentType \"{}\", bytes length {} ",
                requestId,
                systemName,
                tagsStr,
                contentType,
                bytes.length
        );

        MediaType mediaType = MediaType.parseMediaType(contentType);

        log.trace("post(): requestId {}: media type parsed: {}", requestId, mediaType);

        Set<String> tags = parseTags(requestId, tagsStr);

        CachedItem newCachedItem = CachedItem.builder()
                .systemName(systemName)
                .tags(tags)
                .mediaType(mediaType)
                .bytes(bytes)
                .encacheTimestamp(System.currentTimeMillis())
                .build();

        log.trace("post(): requestId {}: new CachedItem constructed: {}", requestId, newCachedItem);

        CachedItem oldCachedItem;
        try {
            oldCachedItem = cacheBackend.encache(newCachedItem);
        } catch (CacheBackendException e) {
            log.error("When encaching for requestId " + requestId, e);
            return ResponseEntity.internalServerError().build();
        }

        if (oldCachedItem != null && log.isTraceEnabled()) {
            log.trace("post(): re");
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
                "+get(): requestId {}: try to get cached data for systemName {}, tagsStr \"{}\"",
                requestId,
                systemName,
                tagsStr
        );

        Set<String> tags = parseTags(requestId, tagsStr);

        Optional<CachedItem> cachedItemOpt;
        try {
            cachedItemOpt = cacheBackend.get(systemName, tags);
        } catch (CacheBackendException e) {
            log.error("-get(): requestId " + requestId + ": Cache backend error", e);
            return ResponseEntity.internalServerError().build();
        }
        if (cachedItemOpt.isEmpty()) {
            log.info("-get(): requestId {}: not found in cache", requestId);
            return ResponseEntity.notFound().build();
        }

        CachedItem cachedItem = cachedItemOpt.get();

        ResponseEntity<byte[]> responseEntity = ResponseEntity
                .status(HttpStatus.OK)
                .contentType(cachedItem.getMediaType())
                .body(cachedItem.getBytes());
        log.debug(
                "-get(): requestId {}: OK with content type {}, bytes length {}",
                requestId,
                cachedItem.getMediaType(),
                cachedItem.getBytes().length
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
