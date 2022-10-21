package com.pischule;

import io.quarkus.runtime.annotations.RegisterForReflection;


@RegisterForReflection
public final class Stats {
    public long links;
    public long visits;

    public Stats(long links, long visits) {
        this.links = links;
        this.visits = visits;
    }
}
