package com.example.jampot.domain.user.dto.response;

import com.example.jampot.domain.common.vo.Role;
import com.example.jampot.domain.user.vo.Provider;

import java.util.List;
import java.util.Optional;

public record MypageResponse (

    String nickName,//닉네임
    String selfIntroduction,
    String profileImgUrl,
    String audioFileUrl,
    Boolean isPublic,
    List<String> sessionList,
    List<String> genreList
){

}
