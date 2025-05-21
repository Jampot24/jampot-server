package com.example.jampot.domain.playRoom.service;

import com.example.jampot.domain.playRoom.domain.PlayRoom;
import com.example.jampot.domain.playRoom.domain.PlayRoomLike;
import com.example.jampot.domain.playRoom.repository.PlayRoomLikeRepository;
import com.example.jampot.domain.playRoom.repository.PlayRoomRepository;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikePlayRoomService {
    private final AuthUtil authUtil;
    private final PlayRoomRepository playRoomRepository;
    private final PlayRoomLikeRepository playRoomLikeRepository;

    @Transactional
    public void likePlayRoom(Long playRoomId) {
        User user = authUtil.getLoggedInUser();

        PlayRoom playRoom = playRoomRepository.findById(playRoomId)
                .orElseThrow(() -> new RuntimeException("합주실을 찾을 수 없습니다."));

        boolean alreadyLiked = playRoomLikeRepository.existsByPlayRoomAndUser(playRoom, user);

        if(!alreadyLiked) {
            PlayRoomLike playRoomLike = PlayRoomLike.create(user, playRoom);
            playRoomLikeRepository.save(playRoomLike);
        }
    }

    @Transactional
    public void unlikePlayRoom(Long playRoomId) {
        User user = authUtil.getLoggedInUser();

        PlayRoom playRoom = playRoomRepository.findById(playRoomId)
                .orElseThrow(() -> new RuntimeException("합주실을 찾을 수 없습니다."));

        playRoomLikeRepository
                .findByPlayRoomAndUser(playRoom, user)
                .ifPresent(playRoomLikeRepository::delete);
    }

}
