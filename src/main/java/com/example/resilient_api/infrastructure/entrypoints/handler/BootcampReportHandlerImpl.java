package com.example.resilient_api.infrastructure.entrypoints.handler;

import com.example.resilient_api.domain.api.BootcampReportServicePort;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.infrastructure.entrypoints.dto.ApiResponse;
import com.example.resilient_api.infrastructure.entrypoints.dto.RegisterReportRequestDTO;
import com.example.resilient_api.infrastructure.entrypoints.mapper.BootcampReportDTOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootcampReportHandlerImpl {

    private static final String X_MESSAGE_ID = "X-Message-Id";
    private final BootcampReportServicePort bootcampReportServicePort;
    private final BootcampReportDTOMapper bootcampReportDTOMapper;

    /**
     * Registra un reporte de bootcamp de forma asíncrona
     * Este endpoint no bloquea esperando que se complete el registro
     */
    public Mono<ServerResponse> registerBootcampReport(ServerRequest request) {
        String messageId = getMessageId(request);
        log.info("=== METRICS HANDLER === Received register bootcamp report request");
        log.info("=== METRICS HANDLER === MessageId: {}", messageId);
        log.info("=== METRICS HANDLER === Path: {}", request.path());
        log.info("=== METRICS HANDLER === Method: {}", request.method());

        return request.bodyToMono(RegisterReportRequestDTO.class)
                .doOnNext(dto -> log.info("=== METRICS HANDLER === Request DTO: {}", dto))
                .flatMap(requestDTO -> {
                    log.info("=== METRICS HANDLER === Processing bootcampId: {}", requestDTO.getBootcampId());

                    // Iniciar el registro de forma asíncrona (Fire and Forget)
                    bootcampReportServicePort.registerBootcampReport(requestDTO.getBootcampId(), messageId)
                            .subscribe(
                                    null,
                                    error -> log.error("Async error registering bootcamp report for bootcampId: {} with messageId: {}",
                                            requestDTO.getBootcampId(), messageId, error),
                                    () -> log.info("Async bootcamp report registration completed for bootcampId: {} with messageId: {}",
                                            requestDTO.getBootcampId(), messageId)
                            );

                    // Responder inmediatamente con 202 Accepted
                    ApiResponse response = ApiResponse.builder()
                            .code("202")
                            .message("Bootcamp report registration initiated")
                            .identifier(messageId)
                            .date(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                            .build();

                    log.info("=== METRICS HANDLER === Returning 202 Accepted");
                    return ServerResponse.accepted().bodyValue(response);
                })
                .doOnSuccess(response -> log.info("Successfully initiated bootcamp report registration with messageId: {}", messageId))
                .onErrorResume(BusinessException.class, ex -> handleBusinessException(ex, messageId))
                .onErrorResume(TechnicalException.class, ex -> handleTechnicalException(ex, messageId))
                .onErrorResume(ex -> handleUnexpectedException(ex, messageId));
    }

    /**
     * Obtiene el bootcamp con mayor cantidad de personas inscritas
     * Incluye toda la información detallada del bootcamp y sus usuarios
     */
    public Mono<ServerResponse> getMostPopularBootcamp(ServerRequest request) {
        String messageId = getMessageId(request);
        log.info("Received get most popular bootcamp request with messageId: {}", messageId);

        return bootcampReportServicePort.getMostPopularBootcamp(messageId)
                .map(bootcampReportDTOMapper::toDTO)
                .flatMap(reportDTO -> ServerResponse.ok().bodyValue(reportDTO))
                .doOnSuccess(response -> log.info("Successfully retrieved most popular bootcamp with messageId: {}", messageId))
                .onErrorResume(BusinessException.class, ex -> handleBusinessException(ex, messageId))
                .onErrorResume(TechnicalException.class, ex -> handleTechnicalException(ex, messageId))
                .onErrorResume(ex -> handleUnexpectedException(ex, messageId));
    }

    private String getMessageId(ServerRequest request) {
        String messageId = request.headers().firstHeader(X_MESSAGE_ID);
        if (messageId == null || messageId.isEmpty()) {
            messageId = UUID.randomUUID().toString();
        }
        return messageId;
    }

    private Mono<ServerResponse> handleBusinessException(BusinessException ex, String messageId) {
        log.warn("Business exception occurred with messageId: {} - {}", messageId, ex.getMessage());
        ApiResponse response = ApiResponse.builder()
                .code(ex.getTechnicalMessage().getCode())
                .message(ex.getTechnicalMessage().getMessage())
                .identifier(messageId)
                .date(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        int statusCode = Integer.parseInt(ex.getTechnicalMessage().getCode());
        return ServerResponse.status(statusCode).bodyValue(response);
    }

    private Mono<ServerResponse> handleTechnicalException(TechnicalException ex, String messageId) {
        log.error("Technical exception occurred with messageId: {} - {}", messageId, ex.getMessage());
        ApiResponse response = ApiResponse.builder()
                .code(ex.getTechnicalMessage().getCode())
                .message(ex.getTechnicalMessage().getMessage())
                .identifier(messageId)
                .date(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        int statusCode = Integer.parseInt(ex.getTechnicalMessage().getCode());
        return ServerResponse.status(statusCode).bodyValue(response);
    }

    private Mono<ServerResponse> handleUnexpectedException(Throwable ex, String messageId) {
        log.error("Unexpected exception occurred with messageId: {}", messageId, ex);
        ApiResponse response = ApiResponse.builder()
                .code("500")
                .message("An unexpected error occurred")
                .identifier(messageId)
                .date(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        return ServerResponse.status(500).bodyValue(response);
    }
}
