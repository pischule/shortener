package com.pischule.model;

import java.time.Instant;

public record Link(
        String id,
        String url,
        String redirect,
        long visits,
        Instant createdAt,
        boolean isOwner
) {
}
