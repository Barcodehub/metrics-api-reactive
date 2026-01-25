package com.example.resilient_api.infrastructure.adapters.externalservice;

import com.example.resilient_api.domain.model.CapacityDetail;
import com.example.resilient_api.domain.spi.CapacityExternalServicePort;
import com.example.resilient_api.infrastructure.adapters.webclient.CapacityWebClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;

@RequiredArgsConstructor
public class CapacityExternalServiceAdapter implements CapacityExternalServicePort {

    private final CapacityWebClient capacityWebClient;

    @Override
    public Flux<CapacityDetail> getCapacitiesWithTechnologies(List<Long> capacityIds, String messageId) {
        return capacityWebClient.getCapacitiesWithTechnologies(capacityIds, messageId);
    }
}
