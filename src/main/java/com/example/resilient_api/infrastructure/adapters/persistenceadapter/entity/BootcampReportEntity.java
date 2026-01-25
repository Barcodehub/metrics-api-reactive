package com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bootcamp_reports")
public class BootcampReportEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long bootcampId;

    private String bootcampName;
    private String bootcampDescription;
    private LocalDate launchDate;
    private Integer duration;
    private Integer capacityCount;
    private Integer technologyCount;

    @Indexed
    private Integer enrolledUsersCount;

    private List<UserEnrollmentEntity> enrolledUsers;
    private List<CapacityDetailEntity> capacities;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
