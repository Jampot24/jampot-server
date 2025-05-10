package com.example.jampot.domain.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreateScheduleRequest(
        @NotNull
        LocalDate date,

        @Schema(type = "string", format = "HH:mm", example = "19:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @NotNull
        LocalTime localTime,

        @NotNull
        List<String> nickNameList

) {
}
