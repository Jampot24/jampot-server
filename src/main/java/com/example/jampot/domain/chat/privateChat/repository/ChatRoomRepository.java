package com.example.jampot.domain.chat.privateChat.repository;


import com.example.jampot.domain.chat.privateChat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
