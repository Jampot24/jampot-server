package com.example.jampot.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
/*
    @MessageMapping("/send")
    @SendTo("/subscribr/chat")
    public ChatMessageResponse sendMessage(@RequestParam(ChatMessageRequest request) {
        chatService.doSomething(request);
        return new ChatMessageResponse(re);
    }

 */
}
