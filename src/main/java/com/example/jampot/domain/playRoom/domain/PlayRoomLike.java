package com.example.jampot.domain.playRoom.domain;

import com.example.jampot.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor
public class PlayRoomLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_rooom_id")
    PlayRoom playRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    User user;

    @Builder(access = AccessLevel.PRIVATE)
    private PlayRoomLike(User user, PlayRoom playRoom) {
        this.user = user;
        this.playRoom = playRoom;
    }

    public static PlayRoomLike create(User user, PlayRoom playRoom) {
        return PlayRoomLike.builder()
                .user(user)
                .playRoom(playRoom)
                .build();
    }
}
