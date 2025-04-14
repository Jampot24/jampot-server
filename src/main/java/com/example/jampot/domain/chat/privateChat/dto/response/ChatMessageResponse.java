package com.example.jampot.domain.chat.privateChat.dto.response;

public record ChatMessageResponse(
        Long senderId,
        String content,
        String date
) {
}
