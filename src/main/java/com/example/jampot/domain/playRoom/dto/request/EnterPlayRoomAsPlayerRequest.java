package com.example.jampot.domain.playRoom.dto.request;

import jakarta.validation.constraints.NotNull;

public record EnterPlayRoomAsPlayerRequest(
        String playerPW,

        @NotNull
        String session
) {
}
