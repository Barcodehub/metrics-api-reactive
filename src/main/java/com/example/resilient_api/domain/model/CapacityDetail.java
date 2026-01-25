package com.example.resilient_api.domain.model;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record CapacityDetail(
        Long capacityId,
        String capacityName,
        List<TechnologyDetail> technologies
) {
}
