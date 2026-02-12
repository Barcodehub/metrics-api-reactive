package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.*;
import com.example.resilient_api.domain.spi.BootcampReportPersistencePort;
import com.example.resilient_api.domain.spi.BootcampExternalServicePort;
import com.example.resilient_api.domain.spi.BootcampInfo;
import com.example.resilient_api.domain.spi.UserExternalServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios actualizados para BootcampReportUseCase
 * Reflejan la estructura real del código de producción
 */
@ExtendWith(MockitoExtension.class)
class BootcampReportUseCaseTest {

    @Mock
    private BootcampReportPersistencePort bootcampReportPersistencePort;

    @Mock
    private BootcampExternalServicePort bootcampExternalServicePort;

    @Mock
    private UserExternalServicePort userExternalServicePort;

    @InjectMocks
    private BootcampReportUseCase bootcampReportUseCase;

    private String messageId;
    private Long bootcampId;
    private BootcampInfo bootcampInfo;
    private BootcampReport completeReport;

    @BeforeEach
    void setUp() {
        messageId = "test-message-id-123";
        bootcampId = 1L;

        // Crear BootcampInfo con datos completos
        TechnologyDetail tech1 = new TechnologyDetail(1L, "Java");
        TechnologyDetail tech2 = new TechnologyDetail(2L, "Spring");

        CapacityDetail capacity1 = new CapacityDetail(1L, "Backend", List.of(tech1, tech2));

        bootcampInfo = new BootcampInfo(
                1L,
                "Java Bootcamp",
                "Complete Java training",
                LocalDate.of(2024, 1, 1),
                90,
                List.of(capacity1)
        );

        // Crear reporte completo
        UserEnrollment user1 = new UserEnrollment(100L, "John Doe", "john@example.com");

        completeReport = BootcampReport.builder()
                .id("report-123")
                .bootcampId(1L)
                .bootcampName("Java Bootcamp")
                .bootcampDescription("Complete Java training")
                .launchDate(LocalDate.of(2024, 1, 1))
                .duration(90)
                .capacityCount(1)
                .technologyCount(2)
                .enrolledUsersCount(1)
                .enrolledUsers(List.of(user1))
                .capacities(List.of(capacity1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void registerBootcampReport_WithValidBootcamp_ShouldSaveAsynchronously() {
        // Arrange
        when(bootcampExternalServicePort.getBootcampById(bootcampId, messageId))
                .thenReturn(Mono.just(bootcampInfo));
        when(bootcampExternalServicePort.getUserIdsByBootcampId(bootcampId, messageId))
                .thenReturn(Flux.just(100L));
        when(userExternalServicePort.getUsersByIds(anyList(), eq(messageId)))
                .thenReturn(Flux.just(new UserEnrollment(100L, "John Doe", "john@example.com")));
        when(bootcampReportPersistencePort.save(any(BootcampReport.class)))
                .thenReturn(Mono.just(completeReport));

        // Act & Assert - Debe completar sin error (fire-and-forget)
        StepVerifier.create(bootcampReportUseCase.registerBootcampReport(bootcampId, messageId))
                .verifyComplete();

        // Verificar que se llamaron los servicios (con un pequeño delay para async)
        verify(bootcampExternalServicePort, timeout(1000)).getBootcampById(bootcampId, messageId);
    }

//    @Test
//    void registerBootcampReport_WithBootcampNotFound_ShouldCompleteWithoutError() {
//        // Arrange - Bootcamp no existe
//        when(bootcampExternalServicePort.getBootcampById(bootcampId, messageId))
//                .thenReturn(Mono.empty());
//
//        // Act & Assert - Fire-and-forget NO debe propagar error
//        StepVerifier.create(bootcampReportUseCase.registerBootcampReport(bootcampId, messageId))
//                .verifyComplete();
//
//        verify(bootcampExternalServicePort).getBootcampById(bootcampId, messageId);
//        verify(bootcampReportPersistencePort, never()).save(any());
//    }

    @Test
    void registerBootcampReport_WithNoEnrolledUsers_ShouldSaveWithZeroUsers() {
        // Arrange - Bootcamp sin usuarios
        when(bootcampExternalServicePort.getBootcampById(bootcampId, messageId))
                .thenReturn(Mono.just(bootcampInfo));
        when(bootcampExternalServicePort.getUserIdsByBootcampId(bootcampId, messageId))
                .thenReturn(Flux.empty()); // Sin usuarios
        when(bootcampReportPersistencePort.save(any(BootcampReport.class)))
                .thenReturn(Mono.just(completeReport));

        // Act & Assert
        StepVerifier.create(bootcampReportUseCase.registerBootcampReport(bootcampId, messageId))
                .verifyComplete();

        verify(bootcampExternalServicePort, timeout(1000)).getBootcampById(bootcampId, messageId);
        verify(userExternalServicePort, never()).getUsersByIds(anyList(), anyString());
    }

    @Test
    void getMostPopularBootcamp_WithExistingReport_ShouldReturnEnrichedReport() {
        // Arrange
        when(bootcampReportPersistencePort.findMostPopularBootcamp())
                .thenReturn(Mono.just(completeReport));
        when(bootcampExternalServicePort.getUserIdsByBootcampId(bootcampId, messageId))
                .thenReturn(Flux.just(100L, 200L)); // 2 usuarios actuales

        UserEnrollment user1 = new UserEnrollment(100L, "John Doe", "john@example.com");
        UserEnrollment user2 = new UserEnrollment(200L, "Jane Smith", "jane@example.com");

        when(userExternalServicePort.getUsersByIds(anyList(), eq(messageId)))
                .thenReturn(Flux.just(user1, user2));

        // Act & Assert
        StepVerifier.create(bootcampReportUseCase.getMostPopularBootcamp(messageId))
                .expectNextMatches(report ->
                        report.bootcampId().equals(bootcampId) &&
                        report.enrolledUsersCount() == 2 &&
                        report.enrolledUsers().size() == 2
                )
                .verifyComplete();

        verify(bootcampReportPersistencePort).findMostPopularBootcamp();
        verify(bootcampExternalServicePort).getUserIdsByBootcampId(bootcampId, messageId);
        verify(userExternalServicePort).getUsersByIds(anyList(), eq(messageId));
    }

    @Test
    void getMostPopularBootcamp_WithNoReports_ShouldThrowBusinessException() {
        // Arrange - No hay reportes
        when(bootcampReportPersistencePort.findMostPopularBootcamp())
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(bootcampReportUseCase.getMostPopularBootcamp(messageId))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.NO_BOOTCAMPS_REPORTED
                )
                .verify();

        verify(bootcampReportPersistencePort).findMostPopularBootcamp();
        verify(bootcampExternalServicePort, never()).getUserIdsByBootcampId(anyLong(), anyString());
    }

    @Test
    void getMostPopularBootcamp_WithNoCurrentUsers_ShouldReturnReportWithZeroUsers() {
        // Arrange - Reporte existe pero ya no tiene usuarios inscritos
        when(bootcampReportPersistencePort.findMostPopularBootcamp())
                .thenReturn(Mono.just(completeReport));
        when(bootcampExternalServicePort.getUserIdsByBootcampId(bootcampId, messageId))
                .thenReturn(Flux.empty()); // Ya no hay usuarios

        // Act & Assert
        StepVerifier.create(bootcampReportUseCase.getMostPopularBootcamp(messageId))
                .expectNextMatches(report ->
                        report.bootcampId().equals(bootcampId) &&
                        report.enrolledUsersCount() == 0 &&
                        report.enrolledUsers().isEmpty()
                )
                .verifyComplete();

        verify(bootcampReportPersistencePort).findMostPopularBootcamp();
        verify(bootcampExternalServicePort).getUserIdsByBootcampId(bootcampId, messageId);
        verify(userExternalServicePort, never()).getUsersByIds(anyList(), anyString());
    }

    @Test
    void registerBootcampReport_WithMultipleCapacitiesAndTechnologies_ShouldCalculateCorrectCounts() {
        // Arrange - Bootcamp con múltiples capacidades y tecnologías
        TechnologyDetail tech1 = new TechnologyDetail(1L, "Java");
        TechnologyDetail tech2 = new TechnologyDetail(2L, "Spring");
        TechnologyDetail tech3 = new TechnologyDetail(3L, "React");

        CapacityDetail capacity1 = new CapacityDetail(1L, "Backend", List.of(tech1, tech2));
        CapacityDetail capacity2 = new CapacityDetail(2L, "Frontend", List.of(tech3));

        BootcampInfo complexBootcamp = new BootcampInfo(
                1L,
                "Full Stack Bootcamp",
                "Complete full stack training",
                LocalDate.of(2024, 1, 1),
                120,
                List.of(capacity1, capacity2) // 2 capacities, 3 technologies total
        );

        when(bootcampExternalServicePort.getBootcampById(bootcampId, messageId))
                .thenReturn(Mono.just(complexBootcamp));
        when(bootcampExternalServicePort.getUserIdsByBootcampId(bootcampId, messageId))
                .thenReturn(Flux.just(100L, 200L, 300L)); // 3 usuarios

        UserEnrollment user1 = new UserEnrollment(100L, "John Doe", "john@example.com");
        UserEnrollment user2 = new UserEnrollment(200L, "Jane Smith", "jane@example.com");
        UserEnrollment user3 = new UserEnrollment(300L, "Bob Johnson", "bob@example.com");

        when(userExternalServicePort.getUsersByIds(anyList(), eq(messageId)))
                .thenReturn(Flux.just(user1, user2, user3));

        when(bootcampReportPersistencePort.save(any(BootcampReport.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act & Assert
        StepVerifier.create(bootcampReportUseCase.registerBootcampReport(bootcampId, messageId))
                .verifyComplete();

        // Verificar que el save fue llamado con los valores correctos
        verify(bootcampReportPersistencePort, timeout(1000)).save(argThat(report ->
                report.capacityCount() == 2 &&
                report.technologyCount() == 3 &&
                report.enrolledUsersCount() == 3
        ));
    }
}
