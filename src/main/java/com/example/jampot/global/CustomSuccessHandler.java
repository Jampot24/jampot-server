package com.example.jampot.global;

import com.example.jampot.domain.auth.dto.response.CustomOAuth2User;
import com.example.jampot.global.properties.LoginProperties;
import com.example.jampot.global.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final LoginProperties loginProperties;

    public CustomSuccessHandler(JWTUtil jwtUtil, LoginProperties loginProperties) {
        this.jwtUtil = jwtUtil;
        this.loginProperties = loginProperties;
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
        String refreshToken = jwtUtil.createJwt(name, role);

        response.addCookie(createCookie("Authorization", accessToken, 60 * 60 * 5)); //5시간
        response.addCookie(createCookie("RefreshToken", refreshToken,  60 * 60 * 24 * 7)); //5일

        response.setHeader("isNewUser", isNewUser.toString()); //신규 회원 여부
        response.setHeader("name", name.toString());
        response.sendRedirect("http://localhost:5173"); //프론트에 jwt 반환
    }

    private Cookie createCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(loginProperties.getCookieSecure());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
