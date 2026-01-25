package com.example.resilient_api.domain.model;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder(toBuilder = true)
public record BootcampReport(
        String id,
        Long bootcampId,
        String bootcampName,
        String bootcampDescription,
        LocalDate launchDate,
        Integer duration,
        Integer capacityCount,
        Integer technologyCount,
        Integer enrolledUsersCount,
        List<UserEnrollment> enrolledUsers,
        List<CapacityDetail> capacities,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
) {
    public BootcampReport {
        if (bootcampId == null) {
            throw new IllegalArgumentException("Bootcamp ID cannot be null");
        }
        if (bootcampName == null || bootcampName.isBlank()) {
            throw new IllegalArgumentException("Bootcamp name cannot be null or empty");
        }
    }
}
