package com.example.resilient_api.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record TechnologyDetail(
        Long technologyId,
        String technologyName
) {
}
