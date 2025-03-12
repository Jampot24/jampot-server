package com.example.jampot.global;

import com.example.jampot.domain.auth.dto.response.CustomOAuth2User;
import com.example.jampot.global.properties.LoginProperties;
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
import java.net.http.HttpHeaders;
import java.util.Collection;
import java.util.Iterator;


@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomSuccessHandler.class);
    private final JWTUtil jwtUtil;
    private final LoginProperties loginProperties;

    public CustomSuccessHandler(JWTUtil jwtUtil, LoginProperties loginProperties) {
        this.jwtUtil = jwtUtil;
        this.loginProperties = loginProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("onAuthenticationSuccess 실행");
        String requestURI = request.getRequestURI();
        logger.info("requestURI:{}",requestURI);

        logger.info("successHandler 시작");
        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String name = customUserDetails.getName(); //provider_providerID
        Boolean isNewUser = customUserDetails.getIsNewUser();//기존회원 or 신규 가입 회원

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        logger.info("successHandler - jwt 생성");
        String accessToken = jwtUtil.createJwt(name, role);
        String refreshToken = jwtUtil.createRefreshToken(name, role);

        response.addCookie(createCookie("AccessToken", accessToken, 60 * 60 * 5)); //5시간
        response.addCookie(createCookie("RefreshToken", refreshToken,  60 * 60 * 24 * 7)); //5일

        response.setHeader("isNewUser", isNewUser.toString()); //신규 회원 여부


        //TODO(로그인 완료 후 리다이렉트 주소 수정)
        response.sendRedirect("https://localhost:5173");
    }

    private Cookie createCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(loginProperties.getCookieSecure());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        //cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
