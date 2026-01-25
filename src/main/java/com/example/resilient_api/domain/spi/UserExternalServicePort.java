package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.UserEnrollment;
import reactor.core.publisher.Flux;

import java.util.List;

public interface UserExternalServicePort {
    Flux<UserEnrollment> getUsersByIds(List<Long> userIds, String messageId);
}
