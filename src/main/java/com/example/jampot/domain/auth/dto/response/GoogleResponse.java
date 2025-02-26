package com.example.jampot.domain.auth.dto.response;

import java.util.Map;

public class GoogleResponse implements OAuth2Response {
    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute){
        this.attribute = attribute;
    }
    @Override
    public String getProvider() {
        return "google";
    }

    //구글에서 사용자 식별에 사용하는 ID
    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }
}
