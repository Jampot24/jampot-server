package com.example.jampot.domain.chat.privateChat.mongodbRepository;

import com.example.jampot.domain.chat.privateChat.domain.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage,String> {
    Optional<ChatMessage> findTopByChatRoomIdOrderByCreateDateDesc(Long chatRoomId);
    int countByChatRoomIdAndReceiverIdAndReadFalse(Long chatRoomId, Long receiverId);
    List<ChatMessage> findByChatRoomIdOrderByCreateDateAsc(Long chatRoomId);

    @Query(value = "{'chatRoomId': ?0, 'receiverId': ?1, 'read': false }")
    List<ChatMessage> findUnreadMessages(Long chatRoomId, Long receiverId);

}
