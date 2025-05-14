package com.example.jampot.domain.playRoom.dto.response;

import java.util.List;

public record PlayRoomStatusResponse (
        List<ParticipantInfo> participantList
){
    public record ParticipantInfo(
            String nickName,
            String session,
            String imgUrl
    ){}
}
