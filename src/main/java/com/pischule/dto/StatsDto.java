package com.pischule.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Objects;

@RegisterForReflection
public record StatsDto(Long links, Long visits) {
    public StatsDto(Long links, Long visits) {
        this.links = Objects.requireNonNullElse(links,0L);
        this.visits = Objects.requireNonNullElse(visits,0L);
    }
}
