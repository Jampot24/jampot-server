package com.example.jampot.domain.auth.dto.response;

//소스에서 유저 정보를 받을 때 사용하는 dto
public interface OAuth2Response {
    String getProvider(); //제공자 (kakao, google)

    // 제공자에서 사용자를 구분할때 사용하는 ID(사용자가 로그인할 때 입력하는 ID와는 다른 것 )
    String getProviderId();

}
