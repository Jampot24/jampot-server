package com.example.jampot.global.security;

import com.example.jampot.domain.auth.dto.response.CustomOAuth2User;
import com.example.jampot.domain.auth.dto.response.UserLoginResponse;
import com.example.jampot.global.properties.CookieProperties;
import com.example.jampot.global.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.CookieProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {
    private static  final Logger logger = LoggerFactory.getLogger(JWTFilter.class);
    private final JWTUtil jwtUtil;
    private static CookieProperties cookieProperties;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 특정 경로에서는 JWT 검증을 건너뜀
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/oauth2") || requestURI.startsWith("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = null;
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName());//getName(): 쿠키의 key 반환
                if (cookie.getName().equals("AccessToken")) {
                    accessToken = cookie.getValue();
                } else if (cookie.getName().equals("RefreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }


        //AccessToken이 만료되었는지 확인
        if (accessToken == null || jwtUtil.isExpired(accessToken)) {

            // 리프레시 토큰도 없는 경우 에러 응답
            if (refreshToken != null && jwtUtil.isValid(refreshToken)) {
                // 액세스 토큰이 만료되었으면 리프레시 토큰을 사용하여 새 토큰 발급
                accessToken = getAccessTokenFromRefresh(refreshToken);


                // 새로운 액세스 토큰을 쿠키에 담아서 클라이언트에 전달
                Cookie newAccessTokenCookie = new Cookie("AccessToken", accessToken);
                newAccessTokenCookie.setMaxAge(60 * 60 * 24 * 5); // 5일
                newAccessTokenCookie.setSecure(true);
                newAccessTokenCookie.setPath("/");
                newAccessTokenCookie.setHttpOnly(true);
                newAccessTokenCookie.setAttribute("SameSite", "None");

                response.addCookie(newAccessTokenCookie);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expired and no valid refresh token found. Please log in again.");
                return;
            }
        }
        updateSecurityContext(accessToken);
        filterChain.doFilter(request, response);
    }

    private String getAccessTokenFromRefresh(String refreshToken) {
        // Refresh Token 검증
        if (!jwtUtil.isValid(refreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        // Refresh Token에서 사용자 정보 추출
        String providerAndId = jwtUtil.getProviderAndId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 새로운 Access Token 생성
        return jwtUtil.createJwt(providerAndId, role.split("_")[1]);
    }

    private void updateSecurityContext(String accessToken){
        String providerAndId = jwtUtil.getProviderAndId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        // 사용자 정보를 담은 DTO 생성
        UserLoginResponse userLoginResponse = new UserLoginResponse();
        userLoginResponse.setProviderAndId(providerAndId);
        userLoginResponse.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userLoginResponse);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());

        //세션에 사용자 정보 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
