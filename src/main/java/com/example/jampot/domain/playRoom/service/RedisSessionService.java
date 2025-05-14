package com.example.jampot.domain.playRoom.service;
import com.example.jampot.domain.playRoom.dto.response.EnterPlayRoomResponse;
import com.example.jampot.domain.playRoom.dto.response.PlayRoomSessionUserStatus;
import com.example.jampot.domain.playRoom.dto.response.SessionState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RedisSessionService {
    private final RedisTemplate<String, String> redisTemplate;
    private final Duration TTL = Duration.ofDays(14);
    private final Logger logger = LoggerFactory.getLogger(RedisSessionService.class);


    //입장, 퇴장, 유저 위치 확인 (관중 포함)
    private String userRoomKey(Long userId) {
        return "user:" + userId + ":playroom";
    }

    //입장, 퇴장,
    private String userSessionKey(Long userId) {
        return "user:" + userId + ":session";
    }

    //Set, 입장, 퇴장 (관중 포함)
    private String roomUserKey(Long playRoomId) {
        return "playroom:" + playRoomId + ":users";
    }

    //합주실 등록, 수정
    private String roomSessionKey(Long playRoomId, String session) {
        return "playroom:" + playRoomId + ":session:" + session;
    }

    //합주실 등록, 수정
    private String sessionMaxKey(Long playRoomId, String session) {
        return "playroom:" + playRoomId + ":session:" + session + ":max";
    }

    //합주실이 사용중인지 확인
    public boolean isPlaying(Long playRoomId) {

        return redisTemplate.hasKey(roomUserKey(playRoomId));

        /*
        Set<String> keys = redisTemplate.keys("playroom:" + playRoomId + ":session:*");
        if(keys.isEmpty()) return false;


        for(String key : keys){

            if (key.endsWith(":max")) continue;
            String[] parts = key.split(":");

            String session = parts[3];

            String countStr = redisTemplate.opsForValue().get(key);
            if(Integer.parseInt(countStr)!=0) return true;
        }
        return false;
        */
    }

    // 유저가 다른 합주실에 참여 중인지 확인
    public boolean isUserInAnyRoom(Long userId) {
        return redisTemplate.hasKey(userRoomKey(userId));
    }



    // 합주실 생성 시 악기(=세션)별 최대 인원 초기화
    public void initSessions(Long playRoomId, Map<String, Integer> sessionLimits) {
        sessionLimits.forEach((session, max) -> {
            redisTemplate.opsForValue().set(roomSessionKey(playRoomId, session), "0");
            redisTemplate.opsForValue().set(sessionMaxKey(playRoomId, session), String.valueOf(max));
        });
        refreshPlayRoomTTL(playRoomId);
    }

    public void updateSessionMax(Long playRoomId, Map<String, Integer> sessionLimits){
        Set<String> keysToDelete = redisTemplate.keys("playroom:" + playRoomId + ":session:*");
        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }

        // 2. 새로 전달된 세션 정보로 초기화
        sessionLimits.forEach((session, max) -> {
            redisTemplate.opsForValue().set(roomSessionKey(playRoomId, session), "0");
            redisTemplate.opsForValue().set(sessionMaxKey(playRoomId, session), String.valueOf(max));
        });
    }

    // 유저 입장 처리 (악기 선택)
    public EnterPlayRoomResponse tryEnterAsPlayer(Long playRoomId, Long userId, String session) {
        if (isUserInAnyRoom(userId)) {
            return new EnterPlayRoomResponse(false, "이미 다른 합주실에 참여 중입니다."); // 이미 다른 합주실 세션에 참여 중
        }

        String currentKey = roomSessionKey(playRoomId, session);
        String maxKey = sessionMaxKey(playRoomId, session);

        String currentVal = redisTemplate.opsForValue().get(currentKey);
        String maxVal = redisTemplate.opsForValue().get(maxKey);

        if (currentVal == null || maxVal == null) {
            return new EnterPlayRoomResponse(false, "해당 악기 세션 정보를 찾을 수 없습니다.");
        }

        int current = Integer.parseInt(currentVal);
        int max = Integer.parseInt(maxVal);

        if (current >= max) {
            return new EnterPlayRoomResponse(false, "해당 악기 세션은 정원이 초과되었습니다."); // 정원 초과
        }

        // 입장 처리
        //TODO(합주실 관련 key는 업데이트 안해도 되는지?)
        redisTemplate.opsForValue().increment(currentKey);
        redisTemplate.opsForValue().set(userSessionKey(userId), session, TTL);
        redisTemplate.opsForValue().set(userRoomKey(userId), playRoomId.toString(), TTL);
        redisTemplate.opsForSet().add(roomUserKey(playRoomId), userId.toString());

        refreshPlayRoomTTL(playRoomId);
        return new EnterPlayRoomResponse(true, "합주실에 입장되었습니다.");
    }

    // 연주자 퇴장 처리
    public void leaveAsPlayer(Long playRoomId, Long userId) {
        String playRoom = redisTemplate.opsForValue().get(userRoomKey(userId));


        if (playRoom==null || !playRoom.equals(playRoomId.toString())) {
            throw new RuntimeException("연주자가 해당 합주실에 존재하지 않아 퇴장 처리가 불가능 합니다.");
        }

        String session = redisTemplate.opsForValue().get(userSessionKey(userId));
        if(session==null){
            throw new RuntimeException("해당 합주실에 연주자로 참여하지 않았습니다.");
        }

        redisTemplate.opsForValue().decrement(roomSessionKey(playRoomId, session));
        redisTemplate.opsForSet().remove(roomUserKey(playRoomId),userId.toString());
        redisTemplate.delete(userRoomKey(userId));
        redisTemplate.delete(userSessionKey(userId));
        refreshPlayRoomTTL(playRoomId);
    }

    // 관객 입장 처리
    public EnterPlayRoomResponse tryEnterAsAudience(Long playRoomId, Long userId) {
        if (isUserInAnyRoom(userId)) {
            return new EnterPlayRoomResponse(false, "이미 다른 합주실에 참여중입니다.");
        }

        redisTemplate.opsForValue().set(userRoomKey(userId), playRoomId.toString(), TTL);
        redisTemplate.opsForSet().add(roomUserKey(playRoomId), userId.toString());

        refreshPlayRoomTTL(playRoomId);
        return new EnterPlayRoomResponse(true, "합주실에 입장되었습니다.");
    }


    // 관중 퇴장 처리
    public void leaveAsAudience(Long playRoomId, Long userId) {

        String playRoom = redisTemplate.opsForValue().get(userRoomKey(userId));
        if (playRoom==null || !playRoom.equals(playRoomId.toString())) {
            throw new RuntimeException("연주자가 해당 합주실에 존재하지 않아 퇴장 처리가 불가능 합니다.");
        }
        redisTemplate.delete(userRoomKey(userId));
        redisTemplate.opsForSet().remove(roomUserKey(playRoomId),userId.toString());
        refreshPlayRoomTTL(playRoomId);
    }




    // 세션(악기) 참여자 수 조회
    public int getSessionCount(Long playRoomId, String session) {
        String value = redisTemplate.opsForValue().get(roomSessionKey(playRoomId, session));
        return value != null ? Integer.parseInt(value) : 0;
    }

    public int getSessionMax(Long playRoomId, String session) {
        String value = redisTemplate.opsForValue().get(sessionMaxKey(playRoomId, session));
        return value != null ? Integer.parseInt(value) : 0;
    }



    public List<String> getAvailableSession(Long playRoomId){
        Set<String> keys = redisTemplate.keys("playroom:" + playRoomId + ":session:*");
        if(keys.isEmpty()) return Collections.emptyList();

        List<String> availableSession = new ArrayList<>();

        for(String key : keys){

            if (key.endsWith(":max")) continue;
            String[] parts = key.split(":");

            String session = parts[3];

            String countStr = redisTemplate.opsForValue().get(key);
            int count = (countStr!=null) ? Integer.parseInt(countStr) : 0;

            String maxStr = redisTemplate.opsForValue().get(key+":max");
            int max = (maxStr!=null) ? Integer.parseInt(maxStr) : 0;

            if(max>count) availableSession.add(session);
        }
        return availableSession;
    }

    public List<SessionState> getSessionState(Long playRoomId){
        Set<String> keys = redisTemplate.keys("playroom:" + playRoomId + ":session:*");
        if(keys.isEmpty()) return Collections.emptyList();

        List<SessionState> sessionStates = new ArrayList<>();

        for(String key : keys){

            if (key.endsWith(":max")) continue;
            String[] parts = key.split(":");

            String session = parts[3];

            String countStr = redisTemplate.opsForValue().get(key);
            int count = (countStr!=null) ? Integer.parseInt(countStr) : 0;

            String maxStr = redisTemplate.opsForValue().get(key+":max");
            int max = (maxStr!=null) ? Integer.parseInt(maxStr) : 0;
            sessionStates.add(new SessionState(session, count, max));
        }

        return sessionStates;
    }


    public List<PlayRoomSessionUserStatus> getParticipants(Long playRoomId) {
        String key = "playroom:" + playRoomId + ":users";

        Set<String> values = redisTemplate.opsForSet().members(key); // Set<String> 반환

        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        return values.stream()
                .map(entry -> {
                    String[] parts = entry.split(":");
                    Long userId = Long.parseLong(parts[0]);
                    String session = redisTemplate.opsForValue().get(userSessionKey(userId));
                    if(session==null) return null;
                    return new PlayRoomSessionUserStatus(userId, session);
                })
                .filter(Objects::nonNull)
                .toList();
    }



    // TTL 갱신 (활동 발생 시)
    private void refreshPlayRoomTTL(Long playRoomId) {
        Set<String> keys = redisTemplate.keys("playroom:" + playRoomId + ":*");
        if (!keys.isEmpty()) {
            keys.forEach(key -> redisTemplate.expire(key, TTL));
        }
    }

    public void deleteRoom(Long playRoomId) {
        Set<String> keys = redisTemplate.keys("playroom:" + playRoomId + ":*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
