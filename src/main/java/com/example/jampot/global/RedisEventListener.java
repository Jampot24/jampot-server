package com.example.jampot.global;

import com.example.jampot.domain.playRoom.service.PlayRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RedisEventListener {
    private final PlayRoomService playRoomService;

    public void handleKeyExpiredMessage(String expiredKey) {
        if(expiredKey.startsWith("playroom:") && expiredKey.contains(":session:")){
            try{
                String[] parts = expiredKey.split(":");
                Long playRoomId = Long.parseLong(parts[1]);
                playRoomService.deletePlayRoomByTTL(playRoomId);
            }catch(Exception e){
                System.err.println("Invalid expired key" + expiredKey);
            }
        }
    }
}
