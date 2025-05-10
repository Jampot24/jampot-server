package com.example.jampot.domain.schedule.domain;

import com.example.jampot.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleParticipant {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;



    @Builder(access = AccessLevel.PRIVATE)
    private ScheduleParticipant(Schedule schedule, User user){
        this.schedule = schedule;
        this.user = user;
    }

    public static ScheduleParticipant createScheduleParticipant(Schedule schedule, User user){
        return ScheduleParticipant.builder()
                                .schedule(schedule)
                                .user(user)
                                .build();
    }
}

