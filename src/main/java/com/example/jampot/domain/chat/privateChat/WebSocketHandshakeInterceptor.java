package com.example.jampot.domain.chat.privateChat;

import com.example.jampot.global.util.AuthUtil;
import com.example.jampot.global.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            Cookie[] cookies = httpServletRequest.getCookies();
            String accessToken = null;

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("AccessToken".equals(cookie.getName())) {
                        accessToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (accessToken != null && jwtUtil.isValid(accessToken)) {
                String providerAndId = jwtUtil.getProviderAndId(accessToken);
                attributes.put("providerAndId", providerAndId); // 세션에 저장
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}


