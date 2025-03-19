package com.example.jampot.domain.user.domain;

import com.example.jampot.domain.common.domain.Genre;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor
public class UserGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    Genre genre;


    @Builder
    private UserGenre(User user, Genre genre) {
        this.user = user;
        this.genre = genre;
    }

    public static UserGenre create(User user, Genre genre) {
        return UserGenre.builder()
                .user(user)
                .genre(genre)
                .build();
    }
}
