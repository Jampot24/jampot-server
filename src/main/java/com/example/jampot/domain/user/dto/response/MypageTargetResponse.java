package com.example.jampot.domain.user.dto.response;

import java.util.List;

public record MypageTargetResponse (
        String nickName,//닉네임
        String selfIntroduction,
        String profileImgUrl,
        String audioFileUrl,
        List<String> sessionList,
        List<String> genreList
){
}
