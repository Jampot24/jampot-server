package com.example.jampot.domain.playRoom.domain;

import com.example.jampot.domain.schedule.domain.Schedule;
import com.example.jampot.domain.common.domain.Genre;
import com.example.jampot.domain.common.domain.Session;
import com.example.jampot.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@RequiredArgsConstructor
public class PlayRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "play_room_id")
    private Long id;

    @Column
    private String name;
    @Column
    private String description;
    @Column
    private String imageUrl;
    @Column
    private Boolean isPlayerLocked; //연주자 참여 비밀번호
    @Column
    private Boolean isAudienceLocked; //관중 참여 비밀번호
    @Column
    private String playerPW;
    @Column
    private String audiencePW;

    @OneToMany(mappedBy = "playRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayRoomSession> playRoomSessionList = new ArrayList<>();

    @OneToMany(mappedBy = "playRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayRoomGenre> playRoomGenreList = new ArrayList<>();

    @OneToMany(mappedBy = "playRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> scheduleList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;


    @Builder(access = AccessLevel.PRIVATE)
    private PlayRoom(String name, String description, String imageUrl,
                     Boolean isPlayerLocking, Boolean isAudienceLocked,
                     String playerPW, String audiencePW, User creator) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isPlayerLocked = isPlayerLocking;
        this.isAudienceLocked = isAudienceLocked;
        this.playerPW = playerPW;
        this.audiencePW = audiencePW;
        this.creator = creator;
    }

    // 🔹 PlayRoom 생성 메서드
    public static PlayRoom createPlayRoom(String name, String description, String imageUrl,
                                          Boolean isPlayerLocking, Boolean isAudienceLocking,
                                          String playerPW, String audiencePW, User creator, List<SessionMaxPair> selectedSessionMaxPairs, List<Genre> selectedGenres) {
        PlayRoom newPlayRoom =  PlayRoom.builder()
                            .name(name)
                            .description(description)
                            .imageUrl(imageUrl)
                            .isPlayerLocking(isPlayerLocking)
                            .isAudienceLocked(isAudienceLocking)
                            .playerPW(playerPW)
                            .audiencePW(audiencePW)
                            .creator(creator)
                            .build();

        newPlayRoom.sessionMaxPairListToPlaRoomSessionList(selectedSessionMaxPairs);
        newPlayRoom.genreListToPlayRoomGenreList(selectedGenres);

        return newPlayRoom;
    }

    // 🔹 PlayRoom 수정 메서드
    public void updatePlayRoom(String name, String description, String imageUrl,
                               Boolean isPlayerLocking, Boolean isAudienceLocking,
                               String playerPW, String audiencePW,
                               List<SessionMaxPair> sessionMaxPairs, List<Genre> genres) {

        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (isPlayerLocking != null) this.isPlayerLocked = isPlayerLocking;
        if (isAudienceLocking != null) this.isAudienceLocked = isAudienceLocking;
        if (playerPW != null) this.playerPW = playerPW;
        if (audiencePW != null) this.audiencePW = audiencePW;

        // 세션과 장르 업데이트
        if (!sessionMaxPairs.isEmpty()) updateSessions(sessionMaxPairs);
        if (!genres.isEmpty()) updateGenres(genres);
    }

    // 🔹 PlayRoom에 세션 업데이트
    private void updateSessions(List<SessionMaxPair> sessionMaxPairs) {
        playRoomSessionList.clear();
        for (SessionMaxPair pair : sessionMaxPairs) {
            playRoomSessionList.add(PlayRoomSession.create(this, pair.session, pair.maxParticipants));
        }
    }

    // 🔹 PlayRoom에 장르 업데이트
    private void updateGenres(List<Genre> genres) {
        playRoomGenreList.clear();
        for (Genre genre : genres) {
            playRoomGenreList.add(PlayRoomGenre.create(this, genre));
        }
    }

    private void genreListToPlayRoomGenreList(List<Genre> selectedGenres) {
        // 새로 받은 장르 목록으로 PlayRoomGenre 객체 생성
        for (Genre genre : selectedGenres) {
            playRoomGenreList.add(PlayRoomGenre.create(this, genre));
        }
    }

    private void sessionMaxPairListToPlaRoomSessionList(List<SessionMaxPair> sessionMaxPairs) {
        for (SessionMaxPair pair : sessionMaxPairs) {
            playRoomSessionList.add(PlayRoomSession.create(this, pair.session, pair.maxParticipants));
        }
    }


    @Getter
    @RequiredArgsConstructor
    public static class SessionMaxPair{
        private final Session session;
        private final int maxParticipants;
    }
}
