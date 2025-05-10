package com.example.jampot.domain.schedule.repository;


import com.example.jampot.domain.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> {

    List<Schedule> findByPlayRoomIdAndDateBetween(Long playRoomId, LocalDate dateAfter, LocalDate dateBefore);

    Optional<Schedule> findByPlayRoomIdAndDateAndStartTime(Long playRoomId, LocalDate date, LocalTime startTime);
}
