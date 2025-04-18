package com.example.jampot.global.util;

import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    //토큰 payload에 저장될 정보:providerAndID, role, 생성일, 만료일

    private SecretKey secretKey;
    private long accessTokenExpiration = 1000L * 60 * 30;  // 30분
    private long refreshTokenExpiration = 1000L * 60 * 60 * 24 * 7;  // 7일


    //JWTUtil 생성자
    public JWTUtil(@Value("${spring.jwt.secret}")String secret){
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }
    public String getProviderAndId(String token){
        try{
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getSubject();
        }catch(Exception e){
            throw new RuntimeException("Invalid JWT token: " + e.getMessage());
        }
    }
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);

    }

    //만료일 확인 메서드
    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }
    // 토큰 유효성 검사
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String createAccessToken(String providerAndId, String role){
        return Jwts.builder()
                .subject(providerAndId) //provider_provider_ID
                .claim("role", "ROLE_" + role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String providerAndId, String role) {
        return Jwts.builder()
                .subject(providerAndId)
                .claim("role", "ROLE_" + role)
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }
}
