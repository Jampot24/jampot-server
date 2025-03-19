package com.example.jampot.global.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Getter
@Component
public class CookieProperties {
    @Value("${cookie.secure}")
    private Boolean cookieSecure;

    @Value("${cookie.samesite}")
    private String cookieSameSite;

    @Value("${swagger.authorizationUrl}")
    private String swaggerAuthorizationUrl;

    @Value("${swagger.tokenUrl}")
    private String swaggerTokenUrl;
}
