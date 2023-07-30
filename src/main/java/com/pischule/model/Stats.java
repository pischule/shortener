package com.pischule.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Stats(Number links, Number visits) {
}
