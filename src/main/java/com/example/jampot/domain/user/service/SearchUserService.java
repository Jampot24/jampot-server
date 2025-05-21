package com.example.jampot.domain.user.service;

import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.dto.response.SearchUserResponse;
import com.example.jampot.domain.user.repository.UserLikeRepository;
import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchUserService {
    private final UserLikeRepository userLikeRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;


    @Transactional(readOnly = true)
    public SearchUserResponse searchUsersByCondition(String nickname, List<String> sessions, List<String> genres) {
        User user = authUtil.getLoggedInUser();
        List<User> users = userRepository.searchByAllConditions(nickname, sessions, genres);

        List<SearchUserResponse.TargetUserProfile> targetUserProfileList = users.stream()
                .filter(targetUser -> !targetUser.getId().equals(user.getId()))
                .filter(User::getIsPublic)
                .map(targetUser -> {
                    List<String> sessionList = targetUser.getUserSessionList().stream()
                            .limit(2)
                            .map(us -> us.getSession().getName())
                            .toList();

                    boolean isToLike = userLikeRepository.existsByFromUserAndToUser(user, targetUser);

                    return new SearchUserResponse.TargetUserProfile(
                            targetUser.getId(),
                            targetUser.getNickName(),
                            targetUser.getSelfIntroduction(),
                            targetUser.getProfileImgUrl(),
                            sessionList,
                            isToLike
                    );
        }).toList();
        return new SearchUserResponse(targetUserProfileList);
    }

    @Transactional(readOnly = true)
    public SearchUserResponse searchUsersByLiked() {
        User user = authUtil.getLoggedInUser();

        List<User> toLikeUsers = userLikeRepository.findToUsersByFromUser(user);

        List<SearchUserResponse.TargetUserProfile> targetUserProfileList = toLikeUsers.stream()
                .filter(User::getIsPublic)
                .map(targetUser -> {
            List<String> sessionList = targetUser.getUserSessionList().stream()
                    .limit(2)
                    .map(us -> us.getSession().getName())
                    .toList();


            return new SearchUserResponse.TargetUserProfile(
                    targetUser.getId(),
                    targetUser.getNickName(),
                    targetUser.getSelfIntroduction(),
                    targetUser.getProfileImgUrl(),
                    sessionList,
                    true
            );
        }).toList();
        return new SearchUserResponse(targetUserProfileList);

    }
}
