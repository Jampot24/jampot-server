package com.example.jampot.domain.chat.privateChat;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomSessionManager {
    private  int memberNum=0;

    public void addUser() {
        memberNum++;
    }
    public void removeUser() {
        memberNum--;
    }

    public int getConnectedUserCount() {
        return memberNum;
    }
}
