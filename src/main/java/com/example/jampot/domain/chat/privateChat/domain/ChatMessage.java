package com.example.jampot.domain.chat.privateChat.domain;

import com.example.jampot.domain.common.domain.BaseEntity;
import com.example.jampot.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document(collection = "chat_message") // MongoDB 컬렉션 지정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class ChatMessage extends BaseEntity {

    @Id
    private String id; // MongoDB에서는 String 타입의 ObjectId를 사용

    @Indexed
    @Field(targetType = FieldType.INT64)
    private Long chatRoomId; // ChatRoom을 직접 참조하지 않고 ID만 저장

    @Field(targetType = FieldType.INT64)
    private Long senderId;
    // User 참조 대신 ID 저장
    @Indexed
    @Field(targetType = FieldType.INT64)
    private Long receiverId;
    // User 참조 대신 ID 저장
    private String content;

    @Indexed
    private boolean read;

    public static ChatMessage create(Long chatRoomId, Long senderId, Long receiverId, String content, boolean read) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .read(read)
                .build();
    }

    public void markAsRead() {
        this.read = true;
    }
}
