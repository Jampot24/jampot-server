package com.example.jampot.domain.chat.privateChat;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomSessionManager {
    private final Map<Long, Integer> roomUserCount = new ConcurrentHashMap<>();

    public void addUser(Long roomId) {
        roomUserCount.put(roomId, roomUserCount.getOrDefault(roomId, 0) + 1);
    }

    public void removeUser(Long roomId) {
        roomUserCount.put(roomId, Math.max(roomUserCount.getOrDefault(roomId, 0) - 1, 0));
    }

    public int getConnectedUserCount(Long roomId) {
        return roomUserCount.getOrDefault(roomId, 0);
    }
}