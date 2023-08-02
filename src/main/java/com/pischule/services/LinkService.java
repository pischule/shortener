package com.pischule.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.pischule.jooq.tables.records.LinkRecord;
import com.pischule.model.Link;
import com.pischule.model.Page;
import com.pischule.model.Stats;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;
import org.jooq.DSLContext;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Objects;

import static com.pischule.jooq.Tables.LINK;
import static org.jooq.impl.DSL.*;

@Transactional
@ApplicationScoped
public class LinkService {

    @Inject
    DSLContext dsl;

    private static final char[] BASE58_ALPHABET =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    private SecureRandom random;

    @Inject
    UriInfo uriInfo;

    @Inject
    SecurityService securityService;

    private static NotFoundException notFound() {
        return new NotFoundException("Link not found");
    }

    @PostConstruct
    void init() {
        // for graalvm builds
        random = new SecureRandom();
    }

    public Link getById(String id) {
        return dsl.selectFrom(LINK)
                .where(LINK.ID.eq(id))
                .fetchOptional(this::recordToDto)
                .orElseThrow(LinkService::notFound);
    }

    public URI incrementVisitsAndGetUri(String id) {
        return dsl.update(LINK)
                .set(LINK.VISITS, LINK.VISITS.plus(1))
                .where(LINK.ID.eq(id))
                .returning(LINK.URL)
                .fetchOptional(LINK.URL)
                .map(URI::create)
                .orElseThrow(LinkService::notFound);
    }

    public Link saveUrl(String url) {
        var id = generateId();
        var validatedUrl = validateUrl(url).toString();
        return dsl.insertInto(LINK, LINK.ID, LINK.URL, LINK.CREATOR)
                .values(id, validatedUrl, securityService.getUserId())
                .returning()
                .fetchOne(this::recordToDto);
    }

    private String generateId() {
        return NanoIdUtils.randomNanoId(random, BASE58_ALPHABET, 6);
    }

    public void updateUrl(Link link, String newUrl) {
        if (link.redirect().equalsIgnoreCase(newUrl)) {
            throw new IllegalArgumentException("Can't be the same as redirect");
        }

        var validatedUrl = validateUrl(newUrl).toString();
        dsl.update(LINK)
                .set(LINK.URL, validatedUrl)
                .where(LINK.ID.eq(link.id()))
                .and(LINK.CREATOR.eq(securityService.getUserId()))
                .execute();
    }

    private URI validateUrl(String url) throws IllegalArgumentException {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("This field cannot be empty");
        }

        if (!url.matches("^https?://.+")) {
            throw new IllegalArgumentException("Doesn't start with http/https");
        }

        if (url.length() > 2048) {
            throw new IllegalArgumentException("Too long (> 2048 symbols)");
        }

        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }

    public void delete(String id) {
        var creator = securityService.getUserId();
        dsl.delete(LINK)
                .where(LINK.ID.eq(id))
                .and(LINK.CREATOR.eq(creator))
                .execute();
    }

    public Page<Link> getOwned(int page, int size) {
        var elements = dsl.selectFrom(LINK)
                .where(LINK.CREATOR.eq(securityService.getUserId()))
                .orderBy(LINK.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch(this::recordToDto);
        var count = dsl.selectCount()
                .from(LINK)
                .where(LINK.CREATOR.eq(securityService.getUserId()))
                .fetchOne(0, int.class);

        return new Page<>(elements, page, size, count);
    }

    public Stats getStats() {
        return dsl.select(count(), coalesce(sum(LINK.VISITS)))
                .from(LINK)
                .fetchOne(r -> new Stats(r.value1().longValue(),
                        Objects.requireNonNullElse(r.value2(), BigInteger.ZERO)));
    }

    private Link recordToDto(LinkRecord r) {
        String absoluteUrl = uriInfo.getBaseUri().toString() + r.getId();
        var userId = securityService.getUserId();
        boolean isOwner = userId != null && userId.equals(r.getCreator());
        return new Link(r.getId(),
                r.getUrl(),
                absoluteUrl,
                r.getVisits(),
                r.getCreatedAt().toInstant(),
                isOwner);
    }
}
