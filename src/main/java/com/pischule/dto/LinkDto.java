package com.pischule.dto;

import io.quarkus.qute.TemplateData;

import java.time.ZonedDateTime;

@TemplateData
public record LinkDto(
        String id,
        String redirect,
        String destination,
        long clicks,
        ZonedDateTime createdAt,
        boolean owner
) {
}
