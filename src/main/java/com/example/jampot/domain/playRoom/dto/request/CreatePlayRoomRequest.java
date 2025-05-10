package com.example.jampot.domain.playRoom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePlayRoomRequest(
        @NotNull(message = "name cannot be null")
        @NotBlank(message = "name cannot be empty or blank")
        @Size(min = 1, message = "name must have at least one character")
        String name,
        String description,
        String imageUrl,
        String playerPW,
        String audiencePW,

        @NotNull
        Boolean isPlayerLocking,
        @NotNull
        Boolean isAudienceLocking,
        @NotNull
        List<SessionMaxPair> sessionMaxPairs,
        @NotNull
        List<String> genreList
) {
        public record SessionMaxPair(
                @NotNull
                String session,

                @NotNull
                Integer maxParticipants
        ){}
}
