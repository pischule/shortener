package com.pischule;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Stats(
        long links,
        long visits
) {
}
