package com.example.jampot.global.config;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.example.jampot.global.properties.LoginProperties;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SwaggerConfig {
    private LoginProperties loginProperties;

    private static final String COOKIE_AUTH_NAME = "Authorization"; // JWT 쿠키 이름
    private static final String OAUTH2_GOOGLE = "oauth2-google"; // OAuth2 인증 스키마 이름
    private  static final String OAUTH2_KAKAO =  "oauth2-kakao";


    public SwaggerConfig(LoginProperties loginProperties) {
        this.loginProperties = loginProperties;
    }

    @Bean
    public OpenAPI openAPI() {
        Server dev_server = new Server();
        dev_server.setUrl("https://jampot.co.kr");

        Server local_server = new Server();
        local_server.setUrl("http://localhost:8080");

        return new OpenAPI()
                .servers(List.of(dev_server, local_server))
                .addSecurityItem(new SecurityRequirement().addList(COOKIE_AUTH_NAME)) // JWT 쿠키 인증 추가
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2_GOOGLE)) // OAuth2 인증 추가
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2_KAKAO))
                .components(new Components()
                        .addSecuritySchemes(COOKIE_AUTH_NAME, cookieAuthScheme())
                        .addSecuritySchemes(OAUTH2_GOOGLE, oauth2Scheme("google"))
                        .addSecuritySchemes(OAUTH2_KAKAO, oauth2Scheme("kakao")));
    }

    /**
     * JWT 쿠키 인증 방식 추가
     */
    private SecurityScheme cookieAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name(COOKIE_AUTH_NAME);
    }


    /**
     * OAuth2 소셜 로그인 방식 추가 (Google, Kakao 등 지원)
     */
    private SecurityScheme oauth2Scheme(String provider) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(loginProperties.getSwaggerAuthorizationUrl() + provider) // 소셜 로그인 엔드포인트
                                .tokenUrl(loginProperties.getSwaggerTokenUrl()+ provider)));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("https://jampot.vercel.app/", "http://localhost:5173", "http://localhost:3000", "http://localhost:8080", "https://jampot.co.kr"));

        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
