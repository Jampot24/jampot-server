package com.example.jampot.global.service;

import com.example.jampot.domain.auth.dto.response.*;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.domain.user.vo.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

//리소스 서버에서 받은 유저 정보를 프론트에 전달
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    public CustomOAuth2UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    //리소스 서버에서 사용자 정보를 받고 기존 회원인지 아닌지 판단
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest); //provider에서 응답한 사용자 정보 객체
            logger.info("OAuth2User: {}", oAuth2User.getAttributes());

            String registrationId = userRequest.getClientRegistration().getRegistrationId();//google인지 kakao인지

            OAuth2Response oAuth2Response = null;
            logger.info("Registration ID: {}", registrationId);

            if (registrationId.equals("kakao")) {
                oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
            } else if (registrationId.equals("google")) {
                oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
            } else {
                throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
            }

            Provider provider = Provider.fromString(oAuth2Response.getProvider());
            String providerID = oAuth2Response.getProviderId();

            String userName = provider + "_" + providerID;
            Optional<User> existData = userRepository.findByProviderAndProviderId(provider, providerID);
            UserLoginResponse userLoginResponse = new UserLoginResponse();


            userLoginResponse.setIsNewUser(existData.isEmpty());
            userLoginResponse.setProviderAndId(userName);
            String role = (existData.isEmpty())? "GUEST" : existData.get().getRole().toString();
            logger.info(role);
            userLoginResponse.setRole(role);

            return new CustomOAuth2User(userLoginResponse);
        } catch (OAuth2AuthenticationException ex) {
            logger.error("Error during OAuth2 authentication", ex);
            throw ex;  // 예외를 그대로 던져서 OAuth2 인증 실패를 처리하도록 할 수 있습니다.
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while loading user", ex);
            throw ex;
        }
    }
}