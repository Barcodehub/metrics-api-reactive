package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.BootcampReport;
import reactor.core.publisher.Mono;

public interface BootcampReportPersistencePort {
    Mono<BootcampReport> save(BootcampReport bootcampReport);
    Mono<BootcampReport> findByBootcampId(Long bootcampId);
    Mono<BootcampReport> findMostPopularBootcamp();
    Mono<Void> updateEnrollmentCount(Long bootcampId, Integer newCount);
}
