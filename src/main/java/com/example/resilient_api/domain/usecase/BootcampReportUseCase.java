package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.api.BootcampReportServicePort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.BootcampReport;
import com.example.resilient_api.domain.model.CapacityDetail;
import com.example.resilient_api.domain.model.UserEnrollment;
import com.example.resilient_api.domain.spi.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso para el manejo de reportes de bootcamps
 * Implementa SOLID: Single Responsibility, Dependency Inversion
 */
@Slf4j
@RequiredArgsConstructor
public class BootcampReportUseCase implements BootcampReportServicePort {

    private final BootcampReportPersistencePort bootcampReportPersistencePort;
    private final BootcampExternalServicePort bootcampExternalServicePort;
    private final UserExternalServicePort userExternalServicePort;

    @Override
    public Mono<Void> registerBootcampReport(Long bootcampId, String messageId) {
        log.info("Starting async bootcamp report registration for bootcampId: {} with messageId: {}", bootcampId, messageId);

        // Ejecutar de forma asíncrona sin bloquear el flujo principal (Fire and Forget)
        return buildBootcampReport(bootcampId, messageId)
                .flatMap(bootcampReportPersistencePort::save)
                .doOnSuccess(saved -> log.info("Bootcamp report saved successfully for bootcampId: {} with messageId: {}", bootcampId, messageId))
                .doOnError(error -> log.error("Error saving bootcamp report for bootcampId: {} with messageId: {}", bootcampId, messageId, error))
                .then(); // Convertir a Mono<Void>
    }

    @Override
    public Mono<BootcampReport> getMostPopularBootcamp(String messageId) {
        log.info("Getting most popular bootcamp with messageId: {}", messageId);

        return bootcampReportPersistencePort.findMostPopularBootcamp()
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.NO_BOOTCAMPS_REPORTED)))
                .flatMap(report -> enrichReportWithCurrentData(report, messageId))
                .doOnSuccess(report -> log.info("Found most popular bootcamp: {} with {} users enrolled with messageId: {}",
                        report.bootcampName(), report.enrolledUsersCount(), messageId))
                .doOnError(error -> log.error("Error getting most popular bootcamp with messageId: {}", messageId, error));
    }

    /**
     * Construye un reporte completo del bootcamp consultando todos los servicios externos
     * Aplica el principio de composición reactiva
     */
    private Mono<BootcampReport> buildBootcampReport(Long bootcampId, String messageId) {
        log.debug("Building bootcamp report for bootcampId: {} with messageId: {}", bootcampId, messageId);

        // 1. Obtener información básica del bootcamp (YA INCLUYE capacidades con tecnologías)
        return bootcampExternalServicePort.getBootcampById(bootcampId, messageId)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_NOT_FOUND)))
                .flatMap(bootcampInfo -> {
                    // 2. Obtener usuarios inscritos
                    Mono<List<Long>> userIdsMono = bootcampExternalServicePort
                            .getUserIdsByBootcampId(bootcampId, messageId)
                            .collectList();

                    return userIdsMono.flatMap(userIds -> {
                        // 3. Obtener información de usuarios si hay inscritos
                        Mono<List<UserEnrollment>> usersMono = userIds.isEmpty()
                                ? Mono.just(List.of())
                                : userExternalServicePort.getUsersByIds(userIds, messageId).collectList();

                        return usersMono.map(users -> {
                            // 4. Calcular métricas
                            List<CapacityDetail> capacities = bootcampInfo.capacities();
                            int technologyCount = capacities.stream()
                                    .mapToInt(cap -> cap.technologies().size())
                                    .sum();

                            log.info("Bootcamp {} has {} capacities and {} technologies",
                                    bootcampId, capacities.size(), technologyCount);

                            // 5. Construir el reporte completo
                            return BootcampReport.builder()
                                    .bootcampId(bootcampInfo.id())
                                    .bootcampName(bootcampInfo.name())
                                    .bootcampDescription(bootcampInfo.description())
                                    .launchDate(bootcampInfo.launchDate())
                                    .duration(bootcampInfo.duration())
                                    .capacityCount(capacities.size())
                                    .technologyCount(technologyCount)
                                    .enrolledUsersCount(users.size())
                                    .enrolledUsers(users)
                                    .capacities(capacities)
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                        });
                    });
                });
    }

    /**
     * Enriquece un reporte existente con datos actuales
     * Útil para refrescar información antes de devolverla
     */
    private Mono<BootcampReport> enrichReportWithCurrentData(BootcampReport report, String messageId) {
        log.debug("Enriching report with current data for bootcampId: {} with messageId: {}", report.bootcampId(), messageId);

        // Obtener usuarios actuales inscritos
        return bootcampExternalServicePort.getUserIdsByBootcampId(report.bootcampId(), messageId)
                .collectList()
                .flatMap(currentUserIds -> {
                    if (currentUserIds.isEmpty()) {
                        return Mono.just(report.toBuilder()
                                .enrolledUsersCount(0)
                                .enrolledUsers(List.of())
                                .updatedAt(LocalDateTime.now())
                                .build());
                    }

                    // Obtener información actualizada de usuarios
                    return userExternalServicePort.getUsersByIds(currentUserIds, messageId)
                            .collectList()
                            .map(users -> report.toBuilder()
                                    .enrolledUsersCount(users.size())
                                    .enrolledUsers(users)
                                    .updatedAt(LocalDateTime.now())
                                    .build());
                });
    }
}
