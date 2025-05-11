package com.example.jampot.domain.playRoom.service;

import com.example.jampot.domain.schedule.dto.response.ScheduleSimpleInfo;
import com.example.jampot.domain.schedule.service.ScheduleService;
import com.example.jampot.domain.common.domain.Genre;
import com.example.jampot.domain.common.domain.Session;
import com.example.jampot.domain.common.repository.GenreRepository;
import com.example.jampot.domain.common.repository.SessionRepository;
import com.example.jampot.domain.playRoom.domain.PlayRoom;
import com.example.jampot.domain.playRoom.dto.request.CreatePlayRoomRequest;
import com.example.jampot.domain.playRoom.dto.request.EditPlayRoomRequest;
import com.example.jampot.domain.playRoom.dto.response.CreatePlayRoomResponse;
import com.example.jampot.domain.playRoom.dto.response.PlayRoomInfoResponse;
import com.example.jampot.domain.playRoom.dto.response.SessionState;
import com.example.jampot.domain.playRoom.dto.response.UploadPlayRoomImgResponse;
import com.example.jampot.domain.playRoom.repository.PlayRoomRepository;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.global.util.AuthUtil;
import com.example.jampot.global.util.PlayRoomImageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayRoomService{
    private final ScheduleService scheduleService;
    private final AuthUtil authUtil;

    private final RedisSessionService redisSessionService;
    private final PlayRoomRepository playRoomRepository;
    private final GenreRepository genreRepository;
    private final SessionRepository sessionRepository;
    private final PlayRoomImageUtil playRoomImageUtil;


    @Transactional
    public CreatePlayRoomResponse createPlayRoom(@Valid CreatePlayRoomRequest request) {
        User creator = authUtil.getLoggedInUser();

        if(!request.name().isEmpty() && playRoomRepository.findByName(request.name()).isPresent()){
            throw new IllegalStateException("이미 사용중인 이름입니다.");
        }


        List<PlayRoom.SessionMaxPair> sessionMaxPairs = request.sessionMaxPairs().stream()
                .map(dto -> {
                    Session session = sessionRepository.findByName(dto.session())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Session not found: " + dto.session()));
                    return new PlayRoom.SessionMaxPair(session, dto.maxParticipants());
                })
                .toList();


        List<Genre> selectedGenres = genreRepository.findByNameIn(request.genreList());

        PlayRoom newPlayRoom = PlayRoom.createPlayRoom(request.name(), request.description(), request.imageUrl(),
                request.isPlayerLocking(), request.isAudienceLocking(), request.playerPW(), request.audiencePW(), creator,
                sessionMaxPairs, selectedGenres
        );
        playRoomRepository.save(newPlayRoom);


        //redis에 세션 정보 등록
        Map<String, Integer> sessionMap = request.sessionMaxPairs().stream()
                .collect(Collectors.toMap(CreatePlayRoomRequest.SessionMaxPair::session,
                        CreatePlayRoomRequest.SessionMaxPair::maxParticipants));

        redisSessionService.initSessions(newPlayRoom.getId(), sessionMap);

        return new CreatePlayRoomResponse(newPlayRoom.getId());
    }


    @Transactional
    public void editPlayRoom(Long playRoomId, EditPlayRoomRequest request) {
        PlayRoom playRoom = playRoomRepository.findById(playRoomId).orElseThrow();
        User loggedInUser = authUtil.getLoggedInUser();

        if(!playRoom.getCreator().equals(loggedInUser)){
            throw new IllegalStateException("합주실 생성자만 합주실을 수정할 수 있습니다.");
        }

        if(request.name() != null){
            Optional<PlayRoom> existingPlayRoom = playRoomRepository.findByName(request.name());

            if(existingPlayRoom.isPresent() && !existingPlayRoom.get().equals(playRoom)){
                throw new IllegalStateException("이미 사용 중인 합주실 이름입니다.");
            }
        }
        List<Genre>  selectedGenres = genreRepository.findByNameIn(request.genreList());


        // 세션 처리 (null-safe)
        List<CreatePlayRoomRequest.SessionMaxPair> requestSessionPairs =
                request.sessionMaxPairs();  // null일 수 있음

        List<PlayRoom.SessionMaxPair> sessionMaxPairs = null;
        Map<String, Integer> sessionMap = null;

        if (requestSessionPairs != null) {
            // Redis: 현재 접속 중인 인원 수 조회
            Map<String, Integer> currentCounts = redisSessionService.getSessionState(playRoomId).stream()
                    .collect(Collectors.toMap(SessionState::sessionName, SessionState::count));

            for (CreatePlayRoomRequest.SessionMaxPair pair : requestSessionPairs) {
                int currentCount = currentCounts.getOrDefault(pair.session(), 0);
                if (pair.maxParticipants() < currentCount) {
                    throw new IllegalArgumentException(String.format(
                            "세션 [%s]에는 이미 %d명이 접속 중입니다. 최대 인원을 %d명 이하로 설정할 수 없습니다.",
                            pair.session(), currentCount, pair.maxParticipants()));
                }
            }

            sessionMaxPairs = requestSessionPairs.stream()
                    .map(dto -> {
                        Session session = sessionRepository.findByName(dto.session())
                                .orElseThrow(() -> new IllegalArgumentException(dto.session() + " 존재하지 않는 세션입니다."));
                        return new PlayRoom.SessionMaxPair(session, dto.maxParticipants());
                    })
                    .toList();

            sessionMap = requestSessionPairs.stream()
                    .collect(Collectors.toMap(CreatePlayRoomRequest.SessionMaxPair::session,
                            CreatePlayRoomRequest.SessionMaxPair::maxParticipants));
        }

        if (sessionMap != null) {
            redisSessionService.updateSessionMax(playRoom.getId(), sessionMap);
        }


        playRoom.updatePlayRoom(
                request.name(),
                request.description(),
                request.imageUrl(),
                request.isPlayerLocking(),
                request.isAudienceLocking(),
                request.playerPW(),
                request.audiencePW(),
                sessionMaxPairs,
                selectedGenres
        );
    }

    @Transactional
    public void deletePlayRoomByTTL(Long playRoomId){
        PlayRoom playRoom = playRoomRepository.findById(playRoomId)
                .orElseThrow(()-> new RuntimeException("id: " + playRoomId + " 합주실을 찾을 수 없습니다."));
        /*
        boolean hasSchedule = scheduleRepository.existsByPlayRoomAndDateAfter(playRoom, LocalDate.now());
        if(hasSchedule){
            throw new IllegalStateException("남아있는 일정이 있어 합주실을 삭제할 수 없습니다.");
        }

        if(redisSessionService.isPlaying(playRoomId)){
            throw new IllegalStateException("현재 연주 중인 합주실을 삭제할 수 없습니다.");
        }
        */
        playRoomRepository.delete(playRoom);
    }

    @Transactional
    public void deletePlayRoom(Long playRoomId){
        User user = authUtil.getLoggedInUser();

        PlayRoom playRoom = playRoomRepository.findById(playRoomId)
                .orElseThrow(()-> new RuntimeException("id: " + playRoomId + " 합주실을 찾을 수 없습니다."));


        if(!playRoom.getCreator().equals(user)){
            throw new AccessDeniedException("합주실 생성자만 합주실을 삭제할 수 있습니다.");
        }

        /*
        boolean hasSchedule = scheduleRepository.existsByPlayRoomAndDateAfter(playRoom, LocalDate.now());
        if(hasSchedule){
            throw new IllegalStateException("남아있는 일정이 있어 합주실을 삭제할 수 없습니다.");
        }
        */


        if(redisSessionService.isPlaying(playRoomId)){
            throw new IllegalStateException("현재 연주 중인 합주실을 삭제할 수 없습니다.");
        }

        playRoomRepository.delete(playRoom);
    }


    //합주실 상세 페이지 정보 반환
    @Transactional(readOnly = true)
    public PlayRoomInfoResponse getPlayRoomInfo(Long playRoomId){
        PlayRoom playRoom = playRoomRepository.findById(playRoomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 합주실입니다."));

        List<SessionState> sessionStates = redisSessionService.getSessionState(playRoomId);

        List<String> genres = playRoom.getPlayRoomGenreList().stream()
                .map(playRoomGenre -> playRoomGenre.getGenre().getName())
                .toList();

        int month = LocalDate.now().getMonthValue();
        List<ScheduleSimpleInfo> schedules = scheduleService.getMonthlySchedule(playRoomId, month);

        return new PlayRoomInfoResponse(playRoom.getName(), playRoom.getDescription(), genres, sessionStates, playRoom.getImageUrl(), schedules);
    }

    @Transactional
    public UploadPlayRoomImgResponse uploadImage(Long playRoomId, MultipartFile file)throws Exception{
        Optional<PlayRoom> playRoom = playRoomRepository.findById(playRoomId);
        if(playRoom.isPresent()){
            User user = authUtil.getLoggedInUser();
            User creator = playRoom.get().getCreator();
            if(!user.equals(creator)){
                throw new Exception("합주실의 생성자만 합주실 대표 이미지를 수정할 수 있습니다.");
            }
        }

        try {
            String fileName = generateImageFileName(playRoomId);
            String imageUrl = playRoomImageUtil.uploadImageFile(file, fileName);
            return new UploadPlayRoomImgResponse(imageUrl);
        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    private String generateImageFileName(Long playRoomId){
        try{
            var md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(playRoomId.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().encodeToString(hash);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
}
