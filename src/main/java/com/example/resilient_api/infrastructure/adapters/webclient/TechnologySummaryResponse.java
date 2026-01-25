package com.example.resilient_api.infrastructure.adapters.webclient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnologySummaryResponse {
    private Long id;
    private String name;
}
