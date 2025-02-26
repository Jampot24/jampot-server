package com.example.jampot.domain.auth.dto.response;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

//리소스 서버에서 받은 값을 프론트에 전달
public class CustomOAuth2User implements OAuth2User {

    private final UserLoginResponse userLoginResponse;

    public CustomOAuth2User(UserLoginResponse userLoginResponse) {
        this.userLoginResponse = userLoginResponse;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userLoginResponse.getRole();
            }
        });
        return collection;
    }


    //사용자 식별 필드
    @Override
    public String getName() {
        return userLoginResponse.getProviderAndId();
    }


    public Boolean getIsNewUser(){
        return userLoginResponse.getIsNewUser();
    }
}
