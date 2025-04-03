package com.example.jampot.domain.chat.privateChat.repository;

import com.example.jampot.domain.chat.privateChat.domain.ChatRoom;
import com.example.jampot.domain.chat.privateChat.domain.ChatRoomParticipant;
import com.example.jampot.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant,Long> {
    List<ChatRoomParticipant> findByUser(User user);

    @Query("SELECT crp.chatRoom.id FROM ChatRoomParticipant crp " +
            "WHERE crp.user.id IN (:userId, :targetUserId) " +
            "GROUP BY crp.chatRoom.id " +
            "HAVING COUNT(crp.chatRoom.id) = 2")
    Optional<Long> findChatRoomIdByUsers(@Param("userId")Long userId, @Param("targetUserId") Long targetUserId);
}
