package com.example.jampot.domain.user.domain;

import com.example.jampot.domain.common.domain.Genre;
import com.example.jampot.domain.common.domain.Session;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "session_id")
    Session session;

    @Builder
    private UserSession(User user, Session session) {
        this.user = user;
        this.session = session;
    }

    public static UserSession create(User user, Session session) {
        return UserSession.builder()
                .user(user)
                .session(session)
                .build();
    }
}
