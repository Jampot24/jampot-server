package com.example.jampot.global.config;

import com.example.jampot.domain.chat.privateChat.domain.ChatMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.example.jampot.domain.chat.privateChat.repository",
        "com.example.jampot.domain.common.repository",
        "com.example.jampot.domain.user",
        "com.example.jampot.domain.playRoom",
        "com.example.jampot.domain.schedule"} )
public class JpaConfig {
}