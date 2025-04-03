package com.example.jampot.global.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry){
        corsRegistry.addMapping("/**")
                .allowedOrigins("https://localhost:3000", "http://localhost:3000", "https://localhost:5173", "https://jampot.co.kr", "https://jampot.co.kr/swagger-ui", "http://localhost:8080/swagger-ui", "ws://localhost:8080", "ws://jampot.co.kr")
                //.allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Set-Cookie")
                .allowCredentials(true);  // 쿠키 포함 허용
    }
}
