package com.example.resilient_api.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record JwtPayload(
        Long userId,
        String email,
        Boolean isAdmin
) {
}
