package com.example.jampot.domain.chat.privateChat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {
    private final ChatRoomSessionManager chatRoomSessionManager;
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();  // e.g., "/topic/1234"
            if (destination != null && destination.startsWith("/topic/")) {
                try {
                    Long roomId = Long.parseLong(destination.replace("/topic/", ""));
                    accessor.getSessionAttributes().put("roomId", roomId);
                    chatRoomSessionManager.addUser(roomId);
                } catch (NumberFormatException e) {
                    // 로그만 남기기
                }
            }
        }

        return message;
    }
}