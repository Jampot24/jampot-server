package com.example.jampot.global.service;

import com.example.jampot.domain.auth.dto.response.*;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

//리소스 서버에서 받은 유저 정보를 프론트에 전달
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    //리소스 서버에서 사용자 정보를 받고 기존 회원인지 아닌지 판단
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest); //provider에서 응답한 사용자 정보 객체
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();//google인지 kakao인지

        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String provider = oAuth2Response.getProvider();
        String providerID = oAuth2Response.getProviderId();

        String userName = provider + "_" + providerID;
        User existData = userRepository.findByProviderAndProviderId(provider, providerID);
        UserLoginResponse userLoginResponse = new UserLoginResponse();

        if (existData == null) {//기존 회원이 아닌경우
            userLoginResponse.setIsNewUser(true);
        }
        else{
            userLoginResponse.setIsNewUser(false);
        }

        userLoginResponse.setProviderAndId(userName);
        userLoginResponse.setRole("USER");

        return new CustomOAuth2User(userLoginResponse);
    }
}