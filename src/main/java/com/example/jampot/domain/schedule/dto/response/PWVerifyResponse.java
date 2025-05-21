package com.example.jampot.domain.schedule.dto.response;

public record PWVerifyResponse(
        boolean verified,
        String message
) {
}
