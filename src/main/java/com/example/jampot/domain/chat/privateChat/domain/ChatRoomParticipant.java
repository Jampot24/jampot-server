package com.example.jampot.domain.chat.privateChat.domain;

import com.example.jampot.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatRoomParticipant(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

    public static ChatRoomParticipant create(ChatRoom chatRoom, User user) {
        ChatRoomParticipant chatParticipant = ChatRoomParticipant.builder()
                                                            .chatRoom(chatRoom)
                                                            .user(user).build();
        return chatParticipant;
    }
}
