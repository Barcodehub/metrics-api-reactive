package com.example.resilient_api.infrastructure.entrypoints;

import com.example.resilient_api.domain.model.BootcampReport;
import com.example.resilient_api.domain.model.CapacityDetail;
import com.example.resilient_api.domain.model.TechnologyDetail;
import com.example.resilient_api.domain.model.UserEnrollment;
import com.example.resilient_api.domain.spi.BootcampInfo;
import com.example.resilient_api.domain.spi.BootcampReportPersistencePort;
import com.example.resilient_api.infrastructure.adapters.webclient.BootcampWebClient;
import com.example.resilient_api.infrastructure.adapters.webclient.UserWebClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test de Integración REAL para Bootcamp Reports:
 * - Levanta TODO el contexto de Spring
 * - Usa una base de datos REAL (MongoDB en memoria o H2)
 * - Mockea SOLO los servicios externos (bootcamp-api, user-api)
 * - Persiste y consulta datos REALES en la base de datos
 * - Prueba el flujo completo end-to-end con persistencia real
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class BootcampReportRealIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BootcampReportPersistencePort bootcampReportPersistencePort;

    // Para limpieza, necesitamos acceso directo al repository
    @Autowired
    private com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampReportRepository bootcampReportRepository;

    @MockBean // Mockear la clase concreta para evitar ambigüedad
    private BootcampWebClient bootcampWebClient;

    @MockBean // Mockear la clase concreta
    private UserWebClient userWebClient;

    @AfterEach
    void cleanUp() {
        // Limpieza de la base de datos después de cada test
        // Usar el repository directamente para eliminar todo
        bootcampReportRepository.deleteAll().block();
    }

    @Test
    void registerBootcampReport_WithValidBootcamp_ShouldPersistInDatabase() throws InterruptedException {
        // Arrange - Mockear servicios externos
        Long bootcampId = 1L;
        TechnologyDetail tech1 = new TechnologyDetail(1L, "Java");
        TechnologyDetail tech2 = new TechnologyDetail(2L, "Spring");
        CapacityDetail capacity = new CapacityDetail(1L, "Backend", List.of(tech1, tech2));

        BootcampInfo bootcampInfo = new BootcampInfo(
                bootcampId,
                "Java Bootcamp",
                "Complete Java training",
                LocalDate.of(2024, 1, 1),
                90,
                List.of(capacity)
        );

        UserEnrollment user1 = new UserEnrollment(100L, "John Doe", "john@example.com");
        UserEnrollment user2 = new UserEnrollment(200L, "Jane Smith", "jane@example.com");

        when(bootcampWebClient.getBootcampById(eq(bootcampId), anyString()))
                .thenReturn(Mono.just(bootcampInfo));
        when(bootcampWebClient.getUserIdsByBootcampId(eq(bootcampId), anyString()))
                .thenReturn(Flux.just(100L, 200L));
        when(userWebClient.getUsersByIds(anyList(), anyString()))
                .thenReturn(Flux.just(user1, user2));

        // Act - Registrar reporte (fire-and-forget, responde 202 Accepted)
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser().roles("ADMIN"))
                .post()
                .uri("/metrics/bootcamp/report")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("bootcampId", bootcampId))
                .exchange()
                .expectStatus().isAccepted(); // 202 Accepted para operaciones async

        // Esperar a que la operación asíncrona complete
        Thread.sleep(500);

        // Assert - Verificar que se guardó en la base de datos REAL
        BootcampReport savedReport = bootcampReportPersistencePort
                .findByBootcampId(bootcampId)
                .block();

        assert savedReport != null : "El reporte debería existir en la base de datos";
        assert savedReport.bootcampId().equals(bootcampId);
        assert savedReport.bootcampName().equals("Java Bootcamp");
        assert savedReport.enrolledUsersCount() == 2;
        assert savedReport.capacityCount() == 1;
        assert savedReport.technologyCount() == 2;
        assert savedReport.enrolledUsers().size() == 2;
    }

    @Test
    void registerBootcampReport_WithNoEnrolledUsers_ShouldPersistWithZeroUsers() throws InterruptedException {
        // Arrange - Bootcamp sin usuarios
        Long bootcampId = 3L;
        TechnologyDetail tech = new TechnologyDetail(1L, "Python");
        CapacityDetail capacity = new CapacityDetail(1L, "Data Science", List.of(tech));

        BootcampInfo bootcampInfo = new BootcampInfo(
                bootcampId,
                "Empty Bootcamp",
                "No users enrolled",
                LocalDate.of(2024, 1, 1),
                60,
                List.of(capacity)
        );

        when(bootcampWebClient.getBootcampById(eq(bootcampId), anyString()))
                .thenReturn(Mono.just(bootcampInfo));
        when(bootcampWebClient.getUserIdsByBootcampId(eq(bootcampId), anyString()))
                .thenReturn(Flux.empty()); // Sin usuarios

        // Act - Registrar reporte
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser().roles("ADMIN"))
                .post()
                .uri("/metrics/bootcamp/report")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("bootcampId", bootcampId))
                .exchange()
                .expectStatus().isAccepted();

        // Esperar operación asíncrona
        Thread.sleep(500);

        // Assert - Verificar persistencia con 0 usuarios
        BootcampReport savedReport = bootcampReportPersistencePort
                .findByBootcampId(bootcampId)
                .block();

        assert savedReport != null;
        assert savedReport.enrolledUsersCount() == 0;
        assert savedReport.enrolledUsers().isEmpty();
        assert savedReport.capacityCount() == 1;
        assert savedReport.technologyCount() == 1;
    }

    @Test
    void registerBootcampReport_MultipleBootcamps_ShouldPersistAll() throws InterruptedException {
        // Arrange - Múltiples bootcamps
        TechnologyDetail tech = new TechnologyDetail(1L, "React");
        CapacityDetail capacity = new CapacityDetail(1L, "Frontend", List.of(tech));

        for (long i = 10L; i <= 12L; i++) {
            final Long bootcampId = i;
            BootcampInfo bootcampInfo = new BootcampInfo(
                    bootcampId,
                    "Bootcamp " + bootcampId,
                    "Description " + bootcampId,
                    LocalDate.of(2024, 1, 1),
                    90,
                    List.of(capacity)
            );

            when(bootcampWebClient.getBootcampById(eq(bootcampId), anyString()))
                    .thenReturn(Mono.just(bootcampInfo));
            when(bootcampWebClient.getUserIdsByBootcampId(eq(bootcampId), anyString()))
                    .thenReturn(Flux.just(100L));
            when(userWebClient.getUsersByIds(anyList(), anyString()))
                    .thenReturn(Flux.just(new UserEnrollment(100L, "User", "user@test.com")));

            // Registrar
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser().roles("ADMIN"))
                    .post()
                    .uri("/metrics/bootcamp/report")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("bootcampId", bootcampId))
                    .exchange()
                    .expectStatus().isAccepted();
        }

        // Esperar a que todas las operaciones completen
        Thread.sleep(1000);

        // Assert - Verificar que se guardaron todos
        Long count = bootcampReportRepository.count().block();
        assert count != null && count == 3 : "Deberían existir 3 reportes en la base de datos";
    }

}
