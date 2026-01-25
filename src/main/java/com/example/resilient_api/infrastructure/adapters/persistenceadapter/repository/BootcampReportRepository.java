package com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository;

import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampReportEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BootcampReportRepository extends ReactiveMongoRepository<BootcampReportEntity, String> {

    Mono<BootcampReportEntity> findByBootcampId(Long bootcampId);

    Mono<BootcampReportEntity> findFirstByOrderByEnrolledUsersCountDesc();
}
