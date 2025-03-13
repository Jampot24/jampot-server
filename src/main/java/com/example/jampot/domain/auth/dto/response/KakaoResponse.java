package com.example.jampot.domain.auth.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.juli.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Map;

public class KakaoResponse implements OAuth2Response {
    private static final Logger log = LoggerFactory.getLogger(KakaoResponse.class);
    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        log.info("KakaoResponse attributes: {}", attribute);
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        if (attribute.containsKey("id_token")) {
            return extractSubFromIdToken(attribute.get("id_token").toString());
        } else if (attribute.containsKey("id")) {
            return attribute.get("id").toString();  // 'id'를 사용하여 인증
        } else {
            throw new IllegalArgumentException("Missing 'id_token' and 'id' attributes");
        }
    }

    private String extractSubFromIdToken(String idToken) {
        try {
            // JWT는 "헤더.페이로드.서명" 구조이므로, 페이로드 부분을 가져옴
            String[] tokenParts = idToken.split("\\.");
            if (tokenParts.length < 2) {
                throw new IllegalArgumentException("Invalid ID Token format");
            }

            // Base64 디코딩 후 JSON 파싱
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> payloadData = objectMapper.readValue(payload, Map.class);

            if (!payloadData.containsKey("sub")) {
                throw new IllegalArgumentException("Missing 'sub' field in ID Token");
            }

            return payloadData.get("sub").toString();
        } catch (Exception e) {
            log.error("Error while extracting sub from ID Token: ", e);
            return null;
        }
    }
}
