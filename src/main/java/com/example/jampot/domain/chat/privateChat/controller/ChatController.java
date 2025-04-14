package com.example.jampot.domain.chat.privateChat.controller;

import com.example.jampot.domain.chat.privateChat.Service.ChatService;
import com.example.jampot.domain.chat.privateChat.dto.request.ChatMessageRequest;
import com.example.jampot.domain.chat.privateChat.dto.response.ChatRoomListResponse;
import com.example.jampot.domain.chat.privateChat.dto.response.ChatRoomResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/private-chat")
@Tag(name = "Chat-Private", description = "1:1 채팅 관련 API")
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "연주자 상세 페이지에서 채팅하기 버튼 클릭", description = "상대방 userId로 채팅방 찾기 or 생성하기")
    @GetMapping("/chat-room/find-by/targetId/{userId}")
    public ResponseEntity<Long> enterChatRoomByUserId(@PathVariable Long userId){
        Long chatRoomId = chatService.getChatRoomIdByTargetUser(userId);
        return ResponseEntity.ok(chatRoomId);
    }


    @Operation(summary = "채팅 목록 반환")
    @GetMapping("/chat-room/list")
    public ResponseEntity<ChatRoomListResponse> chatRoomList() {
        ChatRoomListResponse chatRoomListResponse = chatService.getChatRoomList();
        return ResponseEntity.ok(chatRoomListResponse);
    }


    @Operation(summary = "채팅방의 기존 대화 내용 반환")
    @GetMapping("/chat-room/{roomId}/messages")
    public ResponseEntity<ChatRoomResponse> enterChatRoomByRoomId(@DestinationVariable Long roomId){
        ChatRoomResponse chatRoomResponse = chatService.getChatMessages(roomId);
        return ResponseEntity.ok(chatRoomResponse);
    }


    @MessageMapping("/send/{chatRoomId}")
    public void receiveMessage(@DestinationVariable Long chatRoomId,
                               @Payload ChatMessageRequest chatMessageRequest,
                                SimpMessageHeaderAccessor headerAccessor){
        String providerAndId = headerAccessor.getSessionAttributes().get("providerAndId").toString();
        if (headerAccessor.getSessionAttributes() == null) {
            throw new RuntimeException("WebSocket 세션이 존재하지 않습니다.");
        }

        Object providerAndIdObj = headerAccessor.getSessionAttributes().get("providerAndId");
        if (providerAndIdObj == null) {
            throw new RuntimeException("사용자 정보가 없습니다.");
        }

        if (chatMessageRequest == null || chatMessageRequest.content() == null) {
            throw new RuntimeException("메시지 내용이 없습니다.");
        }
        chatService.sendMessage(providerAndId, chatRoomId, chatMessageRequest.content());
    }
}
