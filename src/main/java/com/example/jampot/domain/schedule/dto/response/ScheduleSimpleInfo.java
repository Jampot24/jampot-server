package com.example.jampot.domain.schedule.dto.response;

import java.time.LocalDate;

public record ScheduleSimpleInfo(
        Long id,
        LocalDate date
) {
}
