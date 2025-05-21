package com.example.jampot.domain.schedule.controller;

import com.example.jampot.domain.schedule.dto.request.CreateScheduleRequest;
import com.example.jampot.domain.schedule.dto.request.PWVerifiedRequest;
import com.example.jampot.domain.schedule.dto.request.EditScheduleRequest;
import com.example.jampot.domain.schedule.dto.response.*;
import com.example.jampot.domain.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/schedule")
@Tag(name = "ScheduleManagement", description = "일정 관리 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // TODO(카톡/구글 알림 발송)
    @Operation(summary = "스케줄 생성")
    @PostMapping("/create")
    public ResponseEntity<CreateScheduleResponse> createSchedule(
            @RequestParam Long playRoomId,
            @Valid  @RequestBody CreateScheduleRequest request) {
        try {
            scheduleService.createSchedule(playRoomId, request);
            return ResponseEntity.ok().body(new CreateScheduleResponse("일정 등록 완료"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new CreateScheduleResponse(e.getMessage()));
        }
    }




    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{scheduleId}/delete")
    public ResponseEntity<ScheduleDeleteResponse> scheduleDelete(@PathVariable("scheduleId") Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok().body(new ScheduleDeleteResponse("일정 삭제 완료"));
    }

    @Operation(summary = "일정 상세보기",description = "일정 하나에 대해 올라옴")
    @GetMapping("/{scheduleId}/detailInfo")
    public ResponseEntity<ScheduleDetailInfoResponse> getScheduleInfo(@PathVariable("scheduleId") Long scheduleId) {
        ScheduleDetailInfoResponse response = scheduleService.getScheduleDetailInfo(scheduleId);
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "스케줄 수정")
    @PutMapping("/{scheduleId}/edit")
    public ResponseEntity<Void> editSchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @Valid @RequestBody EditScheduleRequest request) {
        scheduleService.editSchedule(scheduleId, request);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "캘린더 일정 표시", description = "해당하는 달에 등록된 일정 반환")
    @GetMapping("/monthly/simpleInfo")
    public ResponseEntity<List<ScheduleSimpleInfo>> getMonthlySchedule(
            @RequestParam Long playRoomId,
            @RequestParam Integer month) {
        List<ScheduleSimpleInfo> schedules = scheduleService.getMonthlySchedule(playRoomId, month);
        return ResponseEntity.ok(schedules);
    }

    //TODO(추가구현-비번인코딩)
    @Operation(summary = "연주자 비번 확인", description = "비번 있는 경우에만 요청")
    @PostMapping("/{scheduleId}/check-audience-pw")
    public ResponseEntity<PWVerifyResponse> checkAudiencePw(
            @PathVariable Long scheduleId,
            @RequestBody PWVerifiedRequest request) {
        boolean isVerified = scheduleService.verifyPassword(scheduleId, request.audiencePW());

        if (isVerified) {
            return ResponseEntity.ok(new PWVerifyResponse(true, "비밀번호가 확인되었습니다."));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new PWVerifyResponse(false, "비밀번호가 일치하지 않습니다."));
        }
    }
}

