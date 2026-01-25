package com.example.resilient_api.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {
    // Success messages
    REPORT_CREATED("201", "Bootcamp report created successfully", ""),
    REPORT_FOUND("200", "Bootcamp report found", ""),

    // Error messages
    BOOTCAMP_NOT_FOUND("404", "Bootcamp not found", "bootcampId"),
    NO_BOOTCAMPS_REPORTED("404", "No bootcamp reports found", ""),
    REPORT_NOT_FOUND("404", "Report not found for bootcamp", "bootcampId"),

    // External service errors
    BOOTCAMP_SERVICE_ERROR("500", "Error communicating with bootcamp service", ""),
    CAPACITY_SERVICE_ERROR("500", "Error communicating with capacity service", ""),
    USER_SERVICE_ERROR("500", "Error communicating with user service", ""),

    // Database errors
    DATABASE_ERROR("500", "Database operation failed", ""),

    // Security errors
    TOKEN_EXPIRED("401", "Authentication token has expired", "token"),
    TOKEN_INVALID("401", "Invalid authentication token", "token"),
    TOKEN_REQUIRED("401", "Authentication token is required", "Authorization"),
    UNAUTHORIZED_ACTION("403", "You are not authorized to perform this action", ""),

    // Validation errors
    INVALID_BOOTCAMP_ID("400", "Invalid bootcamp ID", "bootcampId");

    private final String code;
    private final String message;
    private final String identifier;
}
