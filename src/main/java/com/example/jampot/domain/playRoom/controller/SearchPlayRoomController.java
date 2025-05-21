package com.example.jampot.domain.playRoom.controller;

import com.example.jampot.domain.playRoom.dto.response.SearchPlayRoomResponse;
import com.example.jampot.domain.playRoom.service.SearchPlayRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search/play-rooms")
@Tag(name = "PlayRoom Search",description = "합주실 검색 API")
public class SearchPlayRoomController {
    private final SearchPlayRoomService searchPlayRoomService;

    @Operation(summary = "합주실 검색", description = "검색 조건(연주자 잠금 여부, 잔여 세션, 장르), 파라미터가 없는 경우 전체 합주실 반환")
    @GetMapping("/condition")
    public ResponseEntity<SearchPlayRoomResponse> searchPlayRooms(
            @RequestParam(required = false) Boolean isPlayerLocked,
            @RequestParam(required = false) List<String> sessionList,
            @RequestParam(required = false) List<String> genreList) {
        SearchPlayRoomResponse response = searchPlayRoomService.searchPlayRoomByCondition(isPlayerLocked, sessionList, genreList);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "합주실 찜 리스트 보기")
    @GetMapping("/liked")
    public ResponseEntity<SearchPlayRoomResponse> searchPlayRooms(){
        SearchPlayRoomResponse response = searchPlayRoomService.searchPlayRoomByLiked();
        return ResponseEntity.ok().body(response);
    }

}
