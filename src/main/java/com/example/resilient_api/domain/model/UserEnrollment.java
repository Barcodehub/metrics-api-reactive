package com.example.resilient_api.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record UserEnrollment(
        Long userId,
        String userName,
        String userEmail
) {
}
