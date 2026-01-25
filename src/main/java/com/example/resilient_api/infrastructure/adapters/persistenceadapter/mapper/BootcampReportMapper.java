package com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper;

import com.example.resilient_api.domain.model.BootcampReport;
import com.example.resilient_api.domain.model.CapacityDetail;
import com.example.resilient_api.domain.model.TechnologyDetail;
import com.example.resilient_api.domain.model.UserEnrollment;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampReportEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.CapacityDetailEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.TechnologyDetailEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.UserEnrollmentEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BootcampReportMapper {

    public BootcampReportEntity toEntity(BootcampReport bootcampReport) {
        if (bootcampReport == null) {
            return null;
        }

        return BootcampReportEntity.builder()
                .id(bootcampReport.id())
                .bootcampId(bootcampReport.bootcampId())
                .bootcampName(bootcampReport.bootcampName())
                .bootcampDescription(bootcampReport.bootcampDescription())
                .launchDate(bootcampReport.launchDate())
                .duration(bootcampReport.duration())
                .capacityCount(bootcampReport.capacityCount())
                .technologyCount(bootcampReport.technologyCount())
                .enrolledUsersCount(bootcampReport.enrolledUsersCount())
                .enrolledUsers(toUserEnrollmentEntities(bootcampReport.enrolledUsers()))
                .capacities(toCapacityDetailEntities(bootcampReport.capacities()))
                .createdAt(bootcampReport.createdAt())
                .updatedAt(bootcampReport.updatedAt())
                .build();
    }

    public BootcampReport toDomain(BootcampReportEntity entity) {
        if (entity == null) {
            return null;
        }

        return BootcampReport.builder()
                .id(entity.getId())
                .bootcampId(entity.getBootcampId())
                .bootcampName(entity.getBootcampName())
                .bootcampDescription(entity.getBootcampDescription())
                .launchDate(entity.getLaunchDate())
                .duration(entity.getDuration())
                .capacityCount(entity.getCapacityCount())
                .technologyCount(entity.getTechnologyCount())
                .enrolledUsersCount(entity.getEnrolledUsersCount())
                .enrolledUsers(toUserEnrollments(entity.getEnrolledUsers()))
                .capacities(toCapacityDetails(entity.getCapacities()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private List<UserEnrollmentEntity> toUserEnrollmentEntities(List<UserEnrollment> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(user -> UserEnrollmentEntity.builder()
                        .userId(user.userId())
                        .userName(user.userName())
                        .userEmail(user.userEmail())
                        .build())
                .collect(Collectors.toList());
    }

    private List<UserEnrollment> toUserEnrollments(List<UserEnrollmentEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(entity -> UserEnrollment.builder()
                        .userId(entity.getUserId())
                        .userName(entity.getUserName())
                        .userEmail(entity.getUserEmail())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CapacityDetailEntity> toCapacityDetailEntities(List<CapacityDetail> capacities) {
        if (capacities == null) {
            return List.of();
        }
        return capacities.stream()
                .map(capacity -> CapacityDetailEntity.builder()
                        .capacityId(capacity.capacityId())
                        .capacityName(capacity.capacityName())
                        .technologies(toTechnologyDetailEntities(capacity.technologies()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<CapacityDetail> toCapacityDetails(List<CapacityDetailEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(entity -> CapacityDetail.builder()
                        .capacityId(entity.getCapacityId())
                        .capacityName(entity.getCapacityName())
                        .technologies(toTechnologyDetails(entity.getTechnologies()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<TechnologyDetailEntity> toTechnologyDetailEntities(List<TechnologyDetail> technologies) {
        if (technologies == null) {
            return List.of();
        }
        return technologies.stream()
                .map(tech -> TechnologyDetailEntity.builder()
                        .technologyId(tech.technologyId())
                        .technologyName(tech.technologyName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TechnologyDetail> toTechnologyDetails(List<TechnologyDetailEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(entity -> TechnologyDetail.builder()
                        .technologyId(entity.getTechnologyId())
                        .technologyName(entity.getTechnologyName())
                        .build())
                .collect(Collectors.toList());
    }
}
