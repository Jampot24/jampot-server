package com.example.jampot.domain.common.domain;

import com.example.jampot.domain.user.domain.UserGenre;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
