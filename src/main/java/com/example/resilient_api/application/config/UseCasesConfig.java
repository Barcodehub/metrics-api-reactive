package com.example.resilient_api.application.config;

import com.example.resilient_api.domain.api.BootcampReportServicePort;
import com.example.resilient_api.domain.spi.*;
import com.example.resilient_api.domain.usecase.BootcampReportUseCase;
import com.example.resilient_api.infrastructure.adapters.externalservice.BootcampExternalServiceAdapter;
import com.example.resilient_api.infrastructure.adapters.externalservice.CapacityExternalServiceAdapter;
import com.example.resilient_api.infrastructure.adapters.externalservice.UserExternalServiceAdapter;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.BootcampReportPersistenceAdapter;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.BootcampReportMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampReportRepository;
import com.example.resilient_api.infrastructure.adapters.webclient.BootcampWebClient;
import com.example.resilient_api.infrastructure.adapters.webclient.CapacityWebClient;
import com.example.resilient_api.infrastructure.adapters.webclient.UserWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {

    private final BootcampReportRepository bootcampReportRepository;
    private final BootcampReportMapper bootcampReportMapper;
    private final BootcampWebClient bootcampWebClient;
    private final CapacityWebClient capacityWebClient;
    private final UserWebClient userWebClient;

    @Bean
    public BootcampReportPersistencePort bootcampReportPersistencePort() {
        return new BootcampReportPersistenceAdapter(bootcampReportRepository, bootcampReportMapper);
    }

    @Bean
    public BootcampExternalServicePort bootcampExternalServicePort() {
        return new BootcampExternalServiceAdapter(bootcampWebClient);
    }

    @Bean
    public CapacityExternalServicePort capacityExternalServicePort() {
        return new CapacityExternalServiceAdapter(capacityWebClient);
    }

    @Bean
    public UserExternalServicePort userExternalServicePort() {
        return new UserExternalServiceAdapter(userWebClient);
    }

    @Bean
    public BootcampReportServicePort bootcampReportServicePort(
            BootcampReportPersistencePort bootcampReportPersistencePort,
            BootcampExternalServicePort bootcampExternalServicePort,
            UserExternalServicePort userExternalServicePort) {
        return new BootcampReportUseCase(
                bootcampReportPersistencePort,
                bootcampExternalServicePort,
                userExternalServicePort
        );
    }
}
