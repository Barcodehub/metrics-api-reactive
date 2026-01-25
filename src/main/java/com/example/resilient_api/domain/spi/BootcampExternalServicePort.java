package com.example.resilient_api.domain.spi;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface BootcampExternalServicePort {
    Mono<BootcampInfo> getBootcampById(Long bootcampId, String messageId);
    Flux<Long> getUserIdsByBootcampId(Long bootcampId, String messageId);
}

