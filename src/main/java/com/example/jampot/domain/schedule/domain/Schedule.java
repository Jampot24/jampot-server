package com.example.jampot.domain.schedule.domain;

import com.example.jampot.domain.playRoom.domain.PlayRoom;
import com.example.jampot.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    PlayRoom playRoom;

    LocalDate date;
    LocalTime startTime;

    @OneToMany(mappedBy = "schedule", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleParticipant> participants = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Schedule(PlayRoom playRoom, LocalDate date, LocalTime startTime) {
        this.playRoom = playRoom;
        this.date = date;
        this.startTime = startTime;
    }

    public static Schedule createSchedule(PlayRoom playRoom, LocalDate date, LocalTime startTime, List<User> users) {
        Schedule newSchedule = Schedule.builder()
            .playRoom(playRoom)
            .date(date)
            .startTime(startTime)
            .build();
        newSchedule.usersToScheduleParticipants(users);
        return newSchedule;
    }

    public void updateSchedule(LocalDate date, LocalTime startTime, List<User> users) {
        if(date != null) this.date = date;
        if(startTime != null) this.startTime = startTime;
        if(!users.isEmpty()) updateParticipants(users);

    }

    private void updateParticipants(List<User> users) {
        participants.clear();
        for(User user : users){
            participants.add(ScheduleParticipant.createScheduleParticipant(this, user));
        }
    }

    private void usersToScheduleParticipants(List<User> users){
        for(User user : users){
            ScheduleParticipant participant = ScheduleParticipant.createScheduleParticipant(this, user);
            participants.add(participant);
        }
    }
}
