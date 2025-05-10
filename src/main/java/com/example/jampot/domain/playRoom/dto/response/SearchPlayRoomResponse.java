package com.example.jampot.domain.playRoom.dto.response;

import java.util.List;

public record SearchPlayRoomResponse(
    List<PlayRoomProfile> playRoomProfileList
) {
    public record PlayRoomProfile(
            Long playRoomId,
            String name,
            String imgUrl,
            List<String> genre,
            List<String> remainSessions,
            boolean isAudienceLocked, //세션 입장에 비번 있는지
            boolean isLiked
    ){
    }
}
