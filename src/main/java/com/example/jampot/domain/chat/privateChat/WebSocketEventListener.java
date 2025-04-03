package com.example.jampot.domain.chat.privateChat;

import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatRoomSessionManager chatRoomSessionManager;


    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {

        chatRoomSessionManager.addUser();
    }

    //사용자가 webSocket 연결을 종료하면 메서드 호출
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        chatRoomSessionManager.removeUser();
    }
}
