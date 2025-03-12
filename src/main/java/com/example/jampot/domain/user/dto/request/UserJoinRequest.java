package com.example.jampot.domain.user.dto.request;

import com.example.jampot.domain.user.vo.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;


public record UserJoinRequest(
    @NotEmpty String nickname,
    List<String> sessionList,
    List<String> genreList,
    @NotNull Boolean isPublic
){}
