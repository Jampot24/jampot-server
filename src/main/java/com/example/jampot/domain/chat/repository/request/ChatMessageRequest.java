package com.example.jampot.domain.chat.repository.request;

public record ChatMessageRequest(
        String username,
        String content
) {
}
