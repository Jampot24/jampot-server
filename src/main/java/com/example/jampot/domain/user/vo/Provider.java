package com.example.jampot.domain.user.vo;

public enum Provider {
    GOOGLE("google"),
    KAKAO("kakao");

    private final String value;

    Provider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // String 값으로 Provider enum 찾기
    public static Provider fromString(String text) {
        for (Provider provider : Provider.values()) {
            if (provider.value.equalsIgnoreCase(text)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown provider: " + text);
    }
}
