package com.example.resilient_api.domain.api;

import com.example.resilient_api.domain.model.BootcampReport;
import reactor.core.publisher.Mono;

public interface BootcampReportServicePort {
    /**
     * Registra un reporte de bootcamp de forma asíncrona
     * @param bootcampId ID del bootcamp a reportar
     * @param messageId ID del mensaje para trazabilidad
     * @return Mono vacío que completa cuando el reporte es registrado
     */
    Mono<Void> registerBootcampReport(Long bootcampId, String messageId);

    /**
     * Obtiene el bootcamp con mayor cantidad de personas inscritas
     * @param messageId ID del mensaje para trazabilidad
     * @return Mono con el reporte del bootcamp más popular
     */
    Mono<BootcampReport> getMostPopularBootcamp(String messageId);
}
