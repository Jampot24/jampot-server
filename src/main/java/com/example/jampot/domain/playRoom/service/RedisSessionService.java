package com.example.jampot.domain.playRoom.service;

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


    // Redis Key 생성
    private String userRoomKey(String userId) {
        return "user:" + userId + ":playroom";
    }

    private String userSessionKey(String userId) {
        return "user:" + userId + ":session";
    }

    private String sessionKey(Long playRoomId, String session) {
        return "playroom:" + playRoomId + ":session:" + session;
    }


    private String sessionMaxKey(Long playRoomId, String session) {
        return "playroom:" + playRoomId + ":session:" + session + ":max";
    }

    //합주실이 사용중인지 확인
    public boolean isPlaying(Long playRoomId){
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
    }

    // 유저가 다른 합주실에 참여 중인지 확인
    public boolean isUserInAnyRoom(String userId) {
        return redisTemplate.hasKey(userRoomKey(userId));
    }

    public Optional<String> getUserRoom(String userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(userSessionKey(userId)));
    }

    public void clearUserRoom(String userId) {
        redisTemplate.delete(userRoomKey(userId));
        redisTemplate.delete(userSessionKey(userId));
    }

    // 합주실 생성 시 악기(=세션)별 최대 인원 초기화
    public void initSessions(Long playRoomId, Map<String, Integer> sessionLimits) {
        sessionLimits.forEach((session, max) -> {
            redisTemplate.opsForValue().set(sessionKey(playRoomId, session), "0");
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
            redisTemplate.opsForValue().set(sessionKey(playRoomId, session), "0");
            redisTemplate.opsForValue().set(sessionMaxKey(playRoomId, session), String.valueOf(max));
        });
    }

    // 유저 입장 처리 (악기 선택)
    public boolean tryEnter(Long playRoomId, String userId, String session) {
        if (isUserInAnyRoom(userId)) {
            return false; // 이미 다른 합주실 세션에 참여 중
        }

        String currentKey = sessionKey(playRoomId, session);
        String maxKey = sessionMaxKey(playRoomId, session);

        String currentVal = redisTemplate.opsForValue().get(currentKey);
        String maxVal = redisTemplate.opsForValue().get(maxKey);

        if (currentVal == null || maxVal == null) {
            throw new IllegalStateException("해당 악기 세션 정보 없음");
        }

        int current = Integer.parseInt(currentVal);
        int max = Integer.parseInt(maxVal);

        if (current >= max) {
            return false; // 정원 초과
        }

        // 입장 처리
        redisTemplate.opsForValue().increment(currentKey);
        redisTemplate.opsForValue().set(userSessionKey(userId), playRoomId.toString(), TTL);
        redisTemplate.opsForValue().set(userSessionKey(userId), session);

        refreshPlayRoomTTL(playRoomId);
        return true;
    }

    // 퇴장 처리
    public void leave(Long playRoomId, String userId) {
        String session = redisTemplate.opsForValue().get(userSessionKey(userId));
        if (session != null) {
            redisTemplate.opsForValue().decrement(sessionKey(playRoomId, session));
        }

        clearUserRoom(userId);
        refreshPlayRoomTTL(playRoomId);
    }

    // 세션(악기) 참여자 수 조회
    public int getSessionCount(Long playRoomId, String session) {
        String value = redisTemplate.opsForValue().get(sessionKey(playRoomId, session));
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

    //TODO(여기서 고치기)
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

    // TTL 갱신 (활동 발생 시)
    private void refreshPlayRoomTTL(Long playRoomId) {
        Set<String> keys = redisTemplate.keys("playroom:" + playRoomId + ":*");
        if (!keys.isEmpty()) {
            keys.forEach(key -> redisTemplate.expire(key, TTL));
        }
    }
}
