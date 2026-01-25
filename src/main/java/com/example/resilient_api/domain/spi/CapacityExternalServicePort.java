package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.CapacityDetail;
import reactor.core.publisher.Flux;

import java.util.List;

public interface CapacityExternalServicePort {
    Flux<CapacityDetail> getCapacitiesWithTechnologies(List<Long> capacityIds, String messageId);
}
