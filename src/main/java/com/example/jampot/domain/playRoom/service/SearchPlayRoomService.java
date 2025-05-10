package com.example.jampot.domain.playRoom.service;

import com.example.jampot.domain.playRoom.domain.PlayRoom;
import com.example.jampot.domain.playRoom.dto.response.SearchPlayRoomResponse;
import com.example.jampot.domain.playRoom.repository.PlayRoomLikeRepository;
import com.example.jampot.domain.playRoom.repository.PlayRoomRepository;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchPlayRoomService {
    private final PlayRoomRepository playRoomRepository;
    private final RedisSessionService redisSessionService;
    private final AuthUtil authUtil;
    private final PlayRoomLikeRepository playRoomLikeRepository;
    private final Logger logger = LoggerFactory.getLogger(SearchPlayRoomService.class);

    @Transactional(readOnly = true)
    //잠금열림, 잔여세션, 장르
    public SearchPlayRoomResponse searchPlayRoomByCondition(Boolean isPlayerLocked, List<String> sessions, List<String> genres) {
        User user = authUtil.getLoggedInUser();

        // null 방지 처리
        sessions = (sessions != null) ? sessions : Collections.emptyList();
        genres = (genres != null) ? genres : Collections.emptyList();

        //1차 피터링: 연주자 비밀번호 여부
        List<PlayRoom> candidateRooms = (isPlayerLocked != null)
                ? playRoomRepository.findByIsPlayerLocked(isPlayerLocked)
                : playRoomRepository.findAll();

        List<SearchPlayRoomResponse.PlayRoomProfile> result = new ArrayList<>();

        for (PlayRoom playRoom : candidateRooms) {
            Long roomId = playRoom.getId();

            //2차 필터링: 선택한 장르를 모두 포함는지 확인
            if(!genres.isEmpty()) {
                List<String> roomGenres = playRoom.getPlayRoomGenreList().stream()
                        .map(playRoomGenre -> playRoomGenre.getGenre().getName())
                        .toList();
                if (!new HashSet<>(roomGenres).containsAll((genres))) continue;
            }

            //3차 필터링
            List<String> availableSessions = redisSessionService.getAvailableSession(roomId);

            if (!sessions.isEmpty()) {
                boolean allMatch = new HashSet<>(availableSessions).containsAll(sessions);
                if (!allMatch) continue;
            }

            result.add(new SearchPlayRoomResponse.PlayRoomProfile(
                    roomId,
                    playRoom.getName(),
                    playRoom.getImageUrl(),
                    playRoom.getPlayRoomGenreList().stream().map(gr -> gr.getGenre().getName()).toList(),
                    availableSessions,
                    playRoom.getIsAudienceLocked(),
                    playRoomLikeRepository.existsByPlayRoomAndUser(playRoom, user)
            ));
        }
        return new SearchPlayRoomResponse(result);
    }

    @Transactional
    public SearchPlayRoomResponse searchPlayRoomByLiked() {
        User user = authUtil.getLoggedInUser();
        List<PlayRoom> playRooms = playRoomLikeRepository.findPlayRoomByUser(user);

        List<SearchPlayRoomResponse.PlayRoomProfile> playRoomProfileList =
                playRooms.stream()
                        .map(playRoom -> {
                            Long playRoomId = playRoom.getId();

                            List<String> genreList = playRoom.getPlayRoomGenreList().stream()
                                    .limit(1)
                                    .map(pg -> pg.getGenre().getName())
                                    .toList();

                            List<String> remainSessions = playRoom.getPlayRoomSessionList().stream()
                                    .filter(session -> {
                                        String sessionName = session.getSession().getName();
                                        int current = redisSessionService.getSessionCount(playRoomId, sessionName);
                                        int max = redisSessionService.getSessionMax(playRoomId, sessionName);
                                        return current < max;
                                    })
                                    .map(session -> session.getSession().getName())
                                    .toList();

                            return new SearchPlayRoomResponse.PlayRoomProfile(
                                    playRoomId,
                                    playRoom.getName(),
                                    playRoom.getImageUrl(),
                                    genreList,
                                    remainSessions,
                                    playRoom.getIsAudienceLocked(),
                                    playRoomLikeRepository.existsByPlayRoomAndUser(playRoom, user)
                            );
                        })
                        .toList();
        return new SearchPlayRoomResponse(playRoomProfileList);

    }


}
