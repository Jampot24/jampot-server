package com.example.jampot.domain.user.dto.response;


import java.util.List;


public record MypageResponse (

    String nickName,//닉네임
    String selfIntroduction,
    String profileImgUrl,
    String audioFileUrl,
    Boolean calenderServiceAgreement,
    Boolean isPublic,
    List<String> sessionList,
    List<String> genreList
){

}
