package com.example.jampot.domain.common.domain;

import com.example.jampot.domain.user.domain.UserSession;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Session {
    @Id
    @Column(name = "session_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<UserSession> userSessions = new ArrayList<>();
}

