package com.example.jampot.domain.auth.dto.response;

import lombok.Getter;
import lombok.Setter;

//리소스서버 -> 소셜로그인 완료 페이지
@Getter
@Setter
public class UserLoginResponse {
    private String role;
    private String providerAndId;
    private Boolean isNewUser;
}
