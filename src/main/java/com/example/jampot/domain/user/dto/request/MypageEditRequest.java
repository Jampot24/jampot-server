package com.example.jampot.domain.user.dto.request;

import java.util.List;

public record MypageEditRequest(
        String nickName,
        String selfIntroduction,
        List<String> sessionList,
        List<String> genreList,
        String profileImageUrl,
        String profileAudioUrl,
        Boolean isPublic
) {}
