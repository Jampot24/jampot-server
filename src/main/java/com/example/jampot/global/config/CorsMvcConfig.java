package com.example.jampot.global.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry){
        corsRegistry.addMapping("/**")
                .allowedOriginPatterns("/swagger-ui/**", "http://localhost:5173/**","https://jampot.vercel.app/**",  "http://localhost:3000/**")  // 모든 Origin 허용 (특히 Swagger 테스트용)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Set-Cookie")
                .allowCredentials(true);  // 쿠키 포함 허용
    }
}
