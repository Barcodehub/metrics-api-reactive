package com.example.resilient_api.infrastructure.entrypoints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BootcampReportDTO {
    private String id;
    private Long bootcampId;
    private String bootcampName;
    private String bootcampDescription;
    private LocalDate launchDate;
    private Integer duration;
    private Integer capacityCount;
    private Integer technologyCount;
    private Integer enrolledUsersCount;
    private List<UserEnrollmentDTO> enrolledUsers;
    private List<CapacityDetailDTO> capacities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
