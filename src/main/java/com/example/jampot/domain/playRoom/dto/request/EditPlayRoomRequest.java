package com.example.jampot.domain.playRoom.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EditPlayRoomRequest(

    @Size(min = 1, message = "name must have at least one character")
    String name,
    String description,
    String imageUrl,
    String playerPW,
    String audiencePW,

    Boolean isPlayerLocking,

    Boolean isAudienceLocking,

    List<CreatePlayRoomRequest.SessionMaxPair> sessionMaxPairs,

    List<String> genreList
) {
        public record SessionMaxPair(
                String session,
                Integer maxParticipants
        ){}
}
