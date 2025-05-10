package com.example.jampot.domain.playRoom.domain;

import com.example.jampot.domain.common.domain.Genre;
import com.example.jampot.domain.common.domain.Session;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.WebProperties;

@Getter
@Entity
@RequiredArgsConstructor
public class PlayRoomSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_room_id")
    private PlayRoom playRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    private int Max;

    @Builder
    private PlayRoomSession(PlayRoom playRoom, Session session, int Max) {
        this.playRoom = playRoom;
        this.session = session;
        this.Max = Max;
    }

    public static PlayRoomSession create(PlayRoom playRoom, Session session, int Max) {
        return PlayRoomSession.builder()
                .playRoom(playRoom)
                .session(session)
                .Max(Max)
                .build();
    }

}
