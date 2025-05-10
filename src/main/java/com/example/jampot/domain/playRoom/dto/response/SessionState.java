package com.example.jampot.domain.playRoom.dto.response;

public record SessionState(
        String sessionName,
        int Max,
        int count
) {
}
