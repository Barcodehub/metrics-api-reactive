package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.CapacityDetail;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record BootcampInfo(
        Long id,
        String name,
        String description,
        LocalDate launchDate,
        Integer duration,
        List<CapacityDetail> capacities // Ahora incluye capacidades completas
) {
}
