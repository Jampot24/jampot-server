package com.example.jampot.domain.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinRequest {
    private String nickname;
    private String genre;
    private String session;
    public String provider;
    public String providerId;
}
