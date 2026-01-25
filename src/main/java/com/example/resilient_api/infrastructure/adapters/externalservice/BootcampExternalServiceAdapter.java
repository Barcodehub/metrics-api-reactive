package com.example.resilient_api.infrastructure.adapters.externalservice;

import com.example.resilient_api.domain.spi.BootcampExternalServicePort;
import com.example.resilient_api.domain.spi.BootcampInfo;
import com.example.resilient_api.infrastructure.adapters.webclient.BootcampWebClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampExternalServiceAdapter implements BootcampExternalServicePort {

    private final BootcampWebClient bootcampWebClient;

    @Override
    public Mono<BootcampInfo> getBootcampById(Long bootcampId, String messageId) {
        return bootcampWebClient.getBootcampById(bootcampId, messageId);
    }

    @Override
    public Flux<Long> getUserIdsByBootcampId(Long bootcampId, String messageId) {
        return bootcampWebClient.getUserIdsByBootcampId(bootcampId, messageId);
    }
}
