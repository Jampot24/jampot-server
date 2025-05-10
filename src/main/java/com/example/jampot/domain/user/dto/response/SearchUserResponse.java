package com.example.jampot.domain.user.dto.response;

import java.util.List;

public record SearchUserResponse(
    List<TargetUserProfile> targetUserProfileList
) {
    public record TargetUserProfile(
            Long userId,
            String nickName,//닉네임
            String selfIntroduction,
            String profileImgUrl,
            List<String> sessionList,
            boolean isLiked
    ){}
}
