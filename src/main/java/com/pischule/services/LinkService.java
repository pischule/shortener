package com.pischule.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.pischule.dto.LinkDto;
import com.pischule.dto.StatsDto;
import com.pischule.entity.Link;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.PostConstruct;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LinkService {
    private static final char[] BASE58_ALPHABET =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    private SecureRandom random;

    @Inject
    UriInfo uriInfo;

    @Inject
    SecurityService securityService;

    @PostConstruct
    void init() {
        random = new SecureRandom();
    }

    @Transactional
    public Link saveUrl(String url) throws IllegalArgumentException {
        var link = new Link();
        link.id = generateId();
        link.url = validateUrl(url).toString();
        link.creator = securityService.getUserId();
        link.persist();
        return link;
    }

    public LinkDto getById(String id) {
        Link link = findOrThrow(id);
        return linkToDto(link);
    }

    @Transactional
    public URI getUrlAndIncrementVisits(String id) {
        Link link = findOrThrow(id);
        link.visits += 1;
        return URI.create(link.url);
    }

    @Transactional
    public void updateUrl(String id, String newUrl) throws IllegalArgumentException {
        Link link = findOrThrow(id);
        if (!isOwner(link)) {
            throw new ForbiddenException("You dont own link " + link.id);
        }
        link.url = validateUrl(newUrl).toString();
    }

    @Transactional
    public void delete(String id) {
        Link link = Link.findById(id);
        if (link == null) {
            return;
        }
        if (!isOwner(link)) {
            throw new ForbiddenException("You dont own link " + link.id);
        }
        link.delete();
    }

    public List<LinkDto> getAllOwned(Page page) {
        List<Link> linkList = Link.find("creator", Sort.descending("createdAt"), securityService.getUserId()).page(page).list();
        return linkList.stream().map(this::linkToDto).toList();
    }

    private Link findOrThrow(String id) {
        Optional<Link> optionalLink = Link.findByIdOptional(id);
        return optionalLink.orElseThrow(() -> new NotFoundException("Link " + id + " not found"));
    }

    private LinkDto linkToDto(Link link) {
        String absoluteUrl = uriInfo.getBaseUri().toString() + link.id;
        return new LinkDto(link.id, absoluteUrl, link.url, link.visits, link.createdAt, isOwner(link));
    }

    private boolean isOwner(Link link) {
        return link.creator != null && link.creator.equals(securityService.getUserId());
    }

    public URI validateUrl(String url) throws IllegalArgumentException {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Cannot be blank");
        }

        if (!url.matches("^https?://.+")) {
            throw new IllegalArgumentException("URL should start with http/https");
        }

        if (url.length() > 2048) {
            throw new IllegalArgumentException("Too long");
        }

        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }

    public String generateId() {
        return NanoIdUtils.randomNanoId(random, BASE58_ALPHABET, 6);
    }

    public StatsDto getStats() {
        return (StatsDto) Link.getEntityManager()
                .createQuery("select new com.pischule.dto.StatsDto(count(1), sum(l.visits)) from Link l")
                .getSingleResult();
    }
}
