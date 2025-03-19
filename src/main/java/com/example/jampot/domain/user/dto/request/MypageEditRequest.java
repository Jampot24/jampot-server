package com.example.jampot.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MypageEditRequest(

        @NotBlank(message = "Nickname cannot be empty or blank")
        @Size(min = 1, message = "Nickname must have at least one character")
        String nickName,
        String selfIntroduction,
        List<String> sessionList,
        List<String> genreList,
        String profileImageUrl,
        String profileAudioUrl,
        Boolean calenderServiceAgreement,
        Boolean isPublic
) {}
