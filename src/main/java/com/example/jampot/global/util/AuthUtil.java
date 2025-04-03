package com.example.jampot.global.util;

import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.domain.user.vo.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.lang.String.valueOf;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    public User getLoggedInUser(){
        String providerAndId = SecurityContextHolder.getContext().getAuthentication().getName();
        String[] parts = providerAndId.split("_");
        Provider provider = Provider.fromString(valueOf(parts[0]));
        String providerId = parts[1];

        Optional<User> user = userRepository.findByProviderAndProviderId(provider, providerId);
        return user.orElse(null);
    }

}
