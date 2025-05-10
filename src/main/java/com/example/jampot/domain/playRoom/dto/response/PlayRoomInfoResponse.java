package com.example.jampot.domain.playRoom.dto.response;

import com.example.jampot.domain.schedule.dto.response.ScheduleSimpleInfo;

import java.util.List;

//합주실 상세보기 페이지에 리턴할 dto
public record PlayRoomInfoResponse(
        String name,
        String description,
        List<String> genreList,
        //세션별 최대인원, 잔여인원 리스트. dto 생성해야함.
        List<SessionState> sessionInfoList,
        String imageUrl,
        List<ScheduleSimpleInfo> scheduleList
) {
}
