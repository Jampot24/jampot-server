package com.example.jampot.domain.user.service;

import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.domain.UserLike;
import com.example.jampot.domain.user.repository.UserLikeRepository;
import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.global.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLikeService {

    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;

    @Transactional
    public void likeUser(Long toUserId) {
        User fromUser = authUtil.getLoggedInUser();

        if (fromUser.getId().equals(toUserId)) {
            throw new IllegalArgumentException("자기 자신은 찜할 수 없습니다.");
        }

        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("상대 유저를 찾을 수 없습니다."));

        boolean alreadyLiked = userLikeRepository.existsByFromUserAndToUser(fromUser, toUser);

        if (!alreadyLiked) {
            UserLike userLike = UserLike.create(fromUser, toUser);
            userLikeRepository.save(userLike);
        }
    }

    @Transactional
    public void unlikeUser(Long toUserId) {
        User fromUser = authUtil.getLoggedInUser();


        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("상대 유저를 찾을 수 없습니다."));

        userLikeRepository
                .findByFromUserAndToUser(fromUser, toUser)
                .ifPresent(userLikeRepository::delete);
    }
}
