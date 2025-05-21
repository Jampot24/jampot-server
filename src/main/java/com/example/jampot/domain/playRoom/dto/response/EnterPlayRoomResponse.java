package com.example.jampot.domain.playRoom.dto.response;

import com.example.jampot.domain.playRoom.domain.PlayRoom;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Setter;

public record EnterPlayRoomResponse(
        boolean success,

        @Nullable
        String message,

        @Setter
        @Nullable
        PlayRoomStatusResponse roomStatus
)
{}