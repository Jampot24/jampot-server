package com.example.jampot.domain.playRoom.domain;

import com.example.jampot.domain.common.domain.Genre;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.domain.UserGenre;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@RequiredArgsConstructor
public class PlayRoomGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_room_id")
    private PlayRoom playRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    Genre genre;

    @Builder
    private PlayRoomGenre(PlayRoom playRoom, Genre genre) {
        this.playRoom = playRoom;
        this.genre = genre;
    }

    public static PlayRoomGenre create(PlayRoom playRoom, Genre genre) {
        return PlayRoomGenre.builder()
                .playRoom(playRoom)
                .genre(genre)
                .build();
    }
}
