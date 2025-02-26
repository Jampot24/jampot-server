package com.example.jampot.global.security;

import com.example.jampot.domain.auth.dto.response.CustomOAuth2User;
import com.example.jampot.domain.auth.dto.response.UserLoginResponse;
import com.example.jampot.global.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = null;
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();

        if(cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName());//getName(): 쿠키의 key 반환
                if (cookie.getName().equals("Authorization")) {
                    accessToken = cookie.getValue();
                }else if("RefreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }
        //Authorization 헤더 검증
        if (accessToken == null){
            System.out.println("AccessToken null");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access token is missing. Please log in.");
            return;
        }

        //AccessToken이 만료되었는지 확인
        if(jwtUtil.isExpired(accessToken)){
            System.out.println("token expired");

            // 액세스 토큰이 만료되었으면 리프레시 토큰을 사용하여 새 토큰 발급
            refreshToken = getRefreshTokenFromRequest(request);

            // 리프레시 토큰도 없는 경우 에러 응답
            if (refreshToken == null || !jwtUtil.isValid(refreshToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expired and no valid refresh token found. Please log in again.");
                return;
        }
            // 리프레시 토큰이 유효한 경우 새로운 액세스 토큰 발급
            String providerAndId = jwtUtil.getProviderAndId(refreshToken); // refresh token에서 사용자 정보 추출
            String role = jwtUtil.getRole(refreshToken);

            // 새로운 액세스 토큰 생성
            String newAccessToken = jwtUtil.createJwt(providerAndId, role);
            String newRefreshToken = jwtUtil.createJwt(providerAndId, role);

            // 새로운 액세스 토큰을 쿠키에 담아서 클라이언트에 전달
            Cookie newAccessTokenCookie = new Cookie("Authorization", newAccessToken);
            newAccessTokenCookie.setHttpOnly(true);
            newAccessTokenCookie.setPath("/");
            newAccessTokenCookie.setMaxAge(60 * 30); // 30분
            response.addCookie(newAccessTokenCookie);


            // 이제 새로운 토큰으로 계속 진행 (아래 인증 로직 진행)
            accessToken = newAccessToken;
        }

        // 토큰이 유효한 경우, 사용자 정보 추출 및 SecurityContext에 설정
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

        filterChain.doFilter(request, response);
    }


    // 리프레시 토큰을 요청에서 추출하는 메서드
    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("RefreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        return refreshToken;
    }
}
