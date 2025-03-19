package com.example.jampot.domain.user.dto.request;

import com.example.jampot.domain.user.vo.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;


public record UserJoinRequest(
    @NotNull(message = "Nickname cannot be null")
    @NotBlank(message = "Nickname cannot be empty or blank")
    @Size(min = 1, message = "Nickname must have at least one character")
    String nickname,
    List<String> sessionList,
    List<String> genreList,
    @NotNull Boolean isPublic
){}
