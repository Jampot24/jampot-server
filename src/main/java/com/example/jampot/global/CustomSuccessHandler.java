package com.example.jampot.global;

import com.example.jampot.domain.auth.dto.response.CustomOAuth2User;
import com.example.jampot.global.properties.CookieProperties;
import com.example.jampot.global.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;


@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomSuccessHandler.class);
    private final JWTUtil jwtUtil;
    private final CookieProperties cookieProperties;

    public CustomSuccessHandler(JWTUtil jwtUtil, CookieProperties loginProperties) {
        this.jwtUtil = jwtUtil;
        this. cookieProperties = loginProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String name = customUserDetails.getName(); //provider_providerID
        Boolean isNewUser = customUserDetails.getIsNewUser();//기존회원 or 신규 가입 회원

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createJwt(name, role);
        String refreshToken = jwtUtil.createRefreshToken(name, role);

        response.addCookie(createCookie("AccessToken", accessToken, 60 * 60 * 5)); //5시간


        response.addCookie(createCookie("RefreshToken", refreshToken,  60 * 60 * 24 * 7)); //7일

        response.setHeader("isNewUser", isNewUser.toString()); //신규 회원 여부

        if(Objects.equals(role, "USER")) response.sendRedirect(cookieProperties.getLoginUserRedirectUrl());
        else if(Objects.equals(role, "GUEST")) response.sendRedirect(cookieProperties.getLoginGuestRedirectUrl());
    }

    private Cookie createCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setSecure( cookieProperties.getCookieSecure());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", cookieProperties.getCookieSameSite());
        return cookie;
    }
}
