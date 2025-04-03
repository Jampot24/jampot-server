package com.example.jampot.domain.chat.privateChat.dto.response;


import java.util.List;

public record ChatRoomListResponse(
    List<ChatRoomInfo> chatRoomInfoList
) {
    public record ChatRoomInfo(
            Long roomId,
            String targetNickname,
            String targetProfileImgUrl,
            String lastMessage,
            String lastMessageTime,
            int unreadCount
    ){}
}
