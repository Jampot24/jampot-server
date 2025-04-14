package com.example.jampot.domain.chat.privateChat;

import com.example.jampot.domain.chat.privateChat.Service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SubscribeEventListener {

    private final ChatService chatService;


    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        Long roomId = (Long) sessionAttributes.get("roomId");
        String providerAndId = (String) sessionAttributes.get("providerAndId");

        if (roomId != null && providerAndId != null) {
            chatService.markMessagesAsRead(roomId, providerAndId);
        } else {
            // 로그 등 예외 처리
            System.out.println("roomId or providerAndId is missing in session");
        }
    }
}