package com.pischule.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.pischule.entity.Link;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.SecureRandom;

@ApplicationScoped
public class LinkService {
    private SecureRandom random;

    @PostConstruct
    void init() {
        random = new SecureRandom();
    }

    @Transactional
    public Link saveUrl(@Valid @NotNull @URL @Length(max = 2048) String url) {
        var link = new Link();
        link.id = generateId();
        link.url = url;
        link.persist();
        return link;
    }


    public String generateId() {
        return NanoIdUtils.randomNanoId(random, NanoIdUtils.DEFAULT_ALPHABET, 5);
    }
}
