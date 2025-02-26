package com.example.jampot.domain.user.service;

import com.example.jampot.domain.auth.dto.request.UserJoinRequest;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.global.util.JWTUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    public UserService(UserRepository userRepository, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String joinUser(UserJoinRequest userJoinRequest){
        User existingUser = userRepository.findByProviderAndProviderId(userJoinRequest.getProvider(), userJoinRequest.getProviderId());

        if(existingUser != null){//기존 회원인 경우
            throw new IllegalStateException("이미 가입된 회원입니다");
        }

        //신규 가입 회원 저장
        User newUser = new User();
        newUser.setNickName(userJoinRequest.getNickname());
        newUser.setProviderId(userJoinRequest.getProviderId());
        newUser.setProvider(userJoinRequest.getProvider());

        userRepository.save(newUser);

        //JWT 발급 (회원가입 후 바로 로그인 상태 유지)
        return jwtUtil.createJwt(newUser.getProvider()+"_"+newUser.getProviderId(), "USER");
    }

}