package com.example.jampot.domain.playRoom.controller;

import com.example.jampot.domain.playRoom.dto.response.LikePlayRoomResponse;
import com.example.jampot.domain.playRoom.dto.response.UnlikePlayRoomResponse;
import com.example.jampot.domain.playRoom.service.LikePlayRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.jampot.domain.playRoom.domain.QPlayRoomLike.playRoomLike;

@RestController
@RequestMapping("/play-room")
@RequiredArgsConstructor
@Tag(name = "PlayRoomLike", description = "합주실 찜 관련 API")
public class LikePlayRoomController {
    private final LikePlayRoomService likePlayRoomService;

    @Operation(summary = "찜하기 기능")
    @PutMapping("/{playRoomId}/likes")
    public ResponseEntity<LikePlayRoomResponse>likePlayRoom(@PathVariable Long playRoomId) {
        likePlayRoomService.likePlayRoom(playRoomId);
        return ResponseEntity.ok().body(new LikePlayRoomResponse("찜 추가 완료"));
    }

    @Operation(summary = "찜 취소 기능")
    @DeleteMapping("/{playRoomId}/likes")
    public ResponseEntity<UnlikePlayRoomResponse> unlikePlayRoom(@PathVariable Long playRoomId) {
        likePlayRoomService.unlikePlayRoom(playRoomId);
        return ResponseEntity.ok().body(new UnlikePlayRoomResponse("찜 삭제 완료"));
    }
}
