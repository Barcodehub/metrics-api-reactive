package com.example.resilient_api.infrastructure.entrypoints.mapper;

import com.example.resilient_api.domain.model.BootcampReport;
import com.example.resilient_api.domain.model.CapacityDetail;
import com.example.resilient_api.domain.model.TechnologyDetail;
import com.example.resilient_api.domain.model.UserEnrollment;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampReportDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacityDetailDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.TechnologyDetailDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.UserEnrollmentDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BootcampReportDTOMapper {

    public BootcampReportDTO toDTO(BootcampReport bootcampReport) {
        if (bootcampReport == null) {
            return null;
        }

        return BootcampReportDTO.builder()
                .id(bootcampReport.id())
                .bootcampId(bootcampReport.bootcampId())
                .bootcampName(bootcampReport.bootcampName())
                .bootcampDescription(bootcampReport.bootcampDescription())
                .launchDate(bootcampReport.launchDate())
                .duration(bootcampReport.duration())
                .capacityCount(bootcampReport.capacityCount())
                .technologyCount(bootcampReport.technologyCount())
                .enrolledUsersCount(bootcampReport.enrolledUsersCount())
                .enrolledUsers(toUserEnrollmentDTOs(bootcampReport.enrolledUsers()))
                .capacities(toCapacityDetailDTOs(bootcampReport.capacities()))
                .createdAt(bootcampReport.createdAt())
                .updatedAt(bootcampReport.updatedAt())
                .build();
    }

    private List<UserEnrollmentDTO> toUserEnrollmentDTOs(List<UserEnrollment> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(user -> UserEnrollmentDTO.builder()
                        .userId(user.userId())
                        .userName(user.userName())
                        .userEmail(user.userEmail())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CapacityDetailDTO> toCapacityDetailDTOs(List<CapacityDetail> capacities) {
        if (capacities == null) {
            return List.of();
        }
        return capacities.stream()
                .map(capacity -> CapacityDetailDTO.builder()
                        .capacityId(capacity.capacityId())
                        .capacityName(capacity.capacityName())
                        .technologies(toTechnologyDetailDTOs(capacity.technologies()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<TechnologyDetailDTO> toTechnologyDetailDTOs(List<TechnologyDetail> technologies) {
        if (technologies == null) {
            return List.of();
        }
        return technologies.stream()
                .map(tech -> TechnologyDetailDTO.builder()
                        .technologyId(tech.technologyId())
                        .technologyName(tech.technologyName())
                        .build())
                .collect(Collectors.toList());
    }
}
