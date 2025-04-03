package com.example.jampot.domain.chat.privateChat.Service;

import com.example.jampot.domain.chat.privateChat.ChatRoomSessionManager;
import com.example.jampot.domain.chat.privateChat.domain.ChatMessage;
import com.example.jampot.domain.chat.privateChat.domain.ChatRoom;
import com.example.jampot.domain.chat.privateChat.domain.ChatRoomParticipant;
import com.example.jampot.domain.chat.privateChat.dto.response.ChatMessageResponse;
import com.example.jampot.domain.chat.privateChat.dto.response.ChatRoomListResponse;
import com.example.jampot.domain.chat.privateChat.dto.response.ChatRoomResponse;
import com.example.jampot.domain.chat.privateChat.mongodbRepository.ChatMessageRepository;
import com.example.jampot.domain.chat.privateChat.repository.ChatRoomParticipantRepository;
import com.example.jampot.domain.chat.privateChat.repository.ChatRoomRepository;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.domain.user.vo.Provider;
import com.example.jampot.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final AuthUtil authUtil;
    private final ChatRoomSessionManager chatRoomSessionManager;
    private final UserRepository userRepository;

    private  Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    public ChatRoomListResponse getChatRoomList() {
        User user = authUtil.getLoggedInUser();
        List<ChatRoomParticipant> chatRoomparticipants = chatRoomParticipantRepository.findByUser(user);

        List<ChatRoomListResponse.ChatRoomInfo> chatRoomInfoList =  chatRoomparticipants.stream().map(chatRoomParticipant-> {
            ChatRoom chatRoom = chatRoomParticipant.getChatRoom();

            //상대방 찾기
            User targetuser = chatRoom.getChatRoomParticipantList().stream()
                                        .map(ChatRoomParticipant::getUser)
                                        .filter(u -> !u.equals(user))
                                        .findFirst()
                                           .orElse(null);

            //마지막 메시지
            ChatMessage lastChatMessage = chatMessageRepository.findTopByChatRoomIdOrderByCreateDateDesc(chatRoom.getId()).orElse(null);
            //마지막 메시지 시간
            String formattedTime = "";
            if (lastChatMessage != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime messageTime = lastChatMessage.getCreateDate();

                // 하루가 지났는지 확인
                Duration duration = Duration.between(messageTime, now);
                if (duration.toDays() >= 1) {
                    // 하루가 지났으면 며칠 전에 보낸 메시지
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    formattedTime = messageTime.format(timeFormatter);
                } else {
                    // 하루 이내이면 시:분 형식으로 출력
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    formattedTime = messageTime.format(timeFormatter);
                }
            }

            int unreadCount = chatMessageRepository.countByChatRoomIdAndReceiverIdAndReadFalse(chatRoom.getId(), user.getId());

            return new ChatRoomListResponse.ChatRoomInfo(
                    chatRoom.getId(),
                    targetuser != null ? targetuser.getNickName() : "알 수 없음",
                    targetuser != null ? targetuser.getProfileImgUrl() : "",
                    lastChatMessage != null ? lastChatMessage.getContent() : "",
                    formattedTime,
                    unreadCount
            );
        }).collect(Collectors.toList());
        return new ChatRoomListResponse(chatRoomInfoList);
    }

    @Transactional
    public Long getChatRoomIdByTargetUser(Long targetUserId) {
        User user = authUtil.getLoggedInUser();
        User targetUser = userRepository.findById(targetUserId).orElse(null);

        if(targetUser == null){
            throw new IllegalArgumentException("상대방이 존재하지 않습니다.");
        }

        Long id = chatRoomParticipantRepository.findChatRoomIdByUsers(user.getId(), targetUserId).orElse(null);
        if(id == null){
            ChatRoom chatRoom = new  ChatRoom();
            chatRoomRepository.save(chatRoom);
            id = chatRoom.getId();

            ChatRoomParticipant chatRoomParticipant1 = ChatRoomParticipant.create(chatRoom,user);
            ChatRoomParticipant chatRoomParticipant2 = ChatRoomParticipant.create(chatRoom,targetUser);
            chatRoomParticipantRepository.save(chatRoomParticipant1);
            chatRoomParticipantRepository.save(chatRoomParticipant2);
        }
        return id;
    }

    @Transactional
    public ChatRoomResponse getChatMessages(Long chatRoomId) {
        User user = authUtil.getLoggedInUser();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        //상대방 찾기
        User targetuser = chatRoom.getChatRoomParticipantList().stream()
                .map(ChatRoomParticipant::getUser)
                .filter(u -> !u.equals(user))
                .findFirst()
                .orElse(null);


        // 채팅방의 메시지 조회 (최신 순으로 정렬)
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdOrderByCreateDateAsc(chatRoom.getId());


        // DTO 변환
        List<ChatRoomResponse.Chat> chatList = chatMessages.stream().map(chatMessage -> {
            return new ChatRoomResponse.Chat(
                    chatMessage.getSenderId(),
                    chatMessage.getReceiverId(),
                    chatMessage.getContent(),
                    chatMessage.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    chatMessage.isRead()
            );
        }).collect(Collectors.toList());
        return new ChatRoomResponse(user.getNickName(), user.getProfileImgUrl(), targetuser.getNickName(), targetuser.getProfileImgUrl(), chatList);
    }

    @Transactional
    public void sendMessage(String providerAndId, Long chatRoomId, String content){

        String[] parts = providerAndId.split("_");
        Provider provider = Provider.fromString(valueOf(parts[0]));
        String providerId = parts[1];
        User sender = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new RuntimeException("채팅방을 찾을 수 없습니다."));

        User receiver = chatRoom.getChatRoomParticipantList().stream()
                .map(ChatRoomParticipant::getUser)
                .filter(u -> !u.equals(sender))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("유효하지 않은 채팅방입니다."));

        logger.info(String.valueOf(chatRoomSessionManager.getConnectedUserCount()));

        //상대방이 채팅방에 접속중인인지 확인
        boolean read = (chatRoomSessionManager.getConnectedUserCount() == 2);

        //메시지 저장
        ChatMessage chatMessage = ChatMessage.create(chatRoom.getId(), sender.getId(), receiver.getId(), content, read);
        chatMessageRepository.save(chatMessage);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = chatMessage.getCreateDate().format(timeFormatter);

        ChatMessageResponse response = new ChatMessageResponse(sender.getId(), content, formattedTime, read);

        messagingTemplate.convertAndSend("/topic/" + chatRoomId, response);
    }

    @Transactional
    public void markMessagesAsRead(Long chatRoomId) {
        User user = authUtil.getLoggedInUser();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));


        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(chatRoom.getId(), user.getId());
        unreadMessages.forEach(ChatMessage::markAsRead);
        chatMessageRepository.saveAll(unreadMessages);

        // 읽음 처리된 메시지들을 그대로 전송
        List<ChatMessageResponse> responseList = unreadMessages.stream()
                .map(chatMessage -> new ChatMessageResponse(
                        chatMessage.getSenderId(),
                        chatMessage.getContent(),
                        chatMessage.getCreateDate().format(DateTimeFormatter.ofPattern("HH:mm")),
                        true  // isRead = true 로 설정
                ))
                .collect(Collectors.toList());


        messagingTemplate.convertAndSend("/topic/" + chatRoomId,responseList);
    }
}
