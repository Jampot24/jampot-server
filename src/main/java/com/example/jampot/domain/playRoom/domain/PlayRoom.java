package com.example.jampot.domain.playRoom.domain;

import jakarta.persistence.*;

@Entity
public class PlayRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "play_room_id")
    private Long id;
}
