package com.pischule;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;

@ApplicationScoped
public class IdUtil {
    private SecureRandom random;

    @PostConstruct
    void init() {
        random = new SecureRandom();
    }

    public String generate() {
        return NanoIdUtils.randomNanoId(random, NanoIdUtils.DEFAULT_ALPHABET, 8);
    }
}
