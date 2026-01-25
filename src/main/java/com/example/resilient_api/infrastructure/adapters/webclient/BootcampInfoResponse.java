package com.example.resilient_api.infrastructure.adapters.webclient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BootcampInfoResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate launchDate;
    private Integer duration;
    private List<CapacityResponse> capacities; // Ahora recibe capacidades completas

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacityResponse {
        private Long id;
        private String name;
        private List<TechnologyResponse> technologies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnologyResponse {
        private Long id;
        private String name;
    }
}
