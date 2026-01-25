package com.example.resilient_api.infrastructure.adapters.persistenceadapter;

import com.example.resilient_api.domain.model.BootcampReport;
import com.example.resilient_api.domain.spi.BootcampReportPersistencePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.BootcampReportMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class BootcampReportPersistenceAdapter implements BootcampReportPersistencePort {

    private final BootcampReportRepository bootcampReportRepository;
    private final BootcampReportMapper bootcampReportMapper;

    @Override
    public Mono<BootcampReport> save(BootcampReport bootcampReport) {
        log.debug("Saving bootcamp report for bootcampId: {}", bootcampReport.bootcampId());

        return bootcampReportRepository.findByBootcampId(bootcampReport.bootcampId())
                .flatMap(existing -> {
                    // Actualizar reporte existente manteniendo el ID de MongoDB
                    var updated = bootcampReportMapper.toEntity(bootcampReport).toBuilder()
                            .id(existing.getId())
                            .build();
                    return bootcampReportRepository.save(updated);
                })
                .switchIfEmpty(
                    // Crear nuevo reporte
                    Mono.defer(() -> bootcampReportRepository.save(bootcampReportMapper.toEntity(bootcampReport)))
                )
                .map(bootcampReportMapper::toDomain)
                .doOnSuccess(saved -> log.debug("Bootcamp report saved successfully with id: {}", saved.id()))
                .doOnError(error -> log.error("Error saving bootcamp report", error));
    }

    @Override
    public Mono<BootcampReport> findByBootcampId(Long bootcampId) {
        log.debug("Finding bootcamp report by bootcampId: {}", bootcampId);

        return bootcampReportRepository.findByBootcampId(bootcampId)
                .map(bootcampReportMapper::toDomain)
                .doOnSuccess(found -> {
                    if (found != null) {
                        log.debug("Found bootcamp report for bootcampId: {}", bootcampId);
                    } else {
                        log.debug("No bootcamp report found for bootcampId: {}", bootcampId);
                    }
                });
    }

    @Override
    public Mono<BootcampReport> findMostPopularBootcamp() {
        log.debug("Finding most popular bootcamp");

        return bootcampReportRepository.findFirstByOrderByEnrolledUsersCountDesc()
                .map(bootcampReportMapper::toDomain)
                .doOnSuccess(found -> {
                    if (found != null) {
                        log.debug("Found most popular bootcamp: {} with {} users",
                                found.bootcampName(), found.enrolledUsersCount());
                    } else {
                        log.debug("No bootcamp reports found");
                    }
                });
    }

    @Override
    public Mono<Void> updateEnrollmentCount(Long bootcampId, Integer newCount) {
        log.debug("Updating enrollment count for bootcampId: {} to {}", bootcampId, newCount);

        return bootcampReportRepository.findByBootcampId(bootcampId)
                .flatMap(entity -> {
                    entity.setEnrolledUsersCount(newCount);
                    entity.setUpdatedAt(java.time.LocalDateTime.now());
                    return bootcampReportRepository.save(entity);
                })
                .then()
                .doOnSuccess(v -> log.debug("Updated enrollment count successfully"))
                .doOnError(error -> log.error("Error updating enrollment count", error));
    }
}
