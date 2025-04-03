package com.example.jampot.domain.chat.privateChat.dto.response;

import java.util.List;

public record ChatRoomResponse(
        String userNickname,
        String userProfileImage,
        String targetNickname,
        String targetProfileImageUrl,
        List<Chat> chatList
) {
    public record Chat(
            Long senderId,   // 송신자 ID
            Long receiverId,
            String content, // 메시지 내용
            String date, // 메시지 전송 시간
            boolean read // 읽음 여부
    ){

    }
}
