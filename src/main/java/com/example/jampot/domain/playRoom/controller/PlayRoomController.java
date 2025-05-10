package com.example.jampot.domain.playRoom.controller;

import com.example.jampot.domain.playRoom.dto.request.CreatePlayRoomRequest;
import com.example.jampot.domain.playRoom.dto.request.EditPlayRoomRequest;
import com.example.jampot.domain.playRoom.dto.response.CreatePlayRoomResponse;
import com.example.jampot.domain.playRoom.dto.response.DeletePlayRoomResponse;
import com.example.jampot.domain.playRoom.dto.response.PlayRoomInfoResponse;
import com.example.jampot.domain.playRoom.dto.response.UploadPlayRoomImgResponse;
import com.example.jampot.domain.playRoom.service.PlayRoomService;
import com.example.jampot.domain.user.dto.response.UploadUserProfileImgResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.ResponseEntity.ok;

@Controller
@RequiredArgsConstructor
@RequestMapping("/play-room")
@Tag(name = "PlayRoom", description = "합주실 관련 API")
public class PlayRoomController {

    private final PlayRoomService playRoomService;

    @Operation(summary = "합주실 생성 API")
    @PostMapping("/create")
    public ResponseEntity<CreatePlayRoomResponse> createPlayRoom(@RequestBody @Valid CreatePlayRoomRequest request) {
        CreatePlayRoomResponse response = playRoomService.createPlayRoom(request);
        return ok(response);
    }

    @Operation(summary = "합주실 삭제 API", description = "생성자만 삭제 가능")
    @DeleteMapping("/{playRoomId}/delete")
    public ResponseEntity<DeletePlayRoomResponse> playRoomDelete(@PathVariable("playRoomId") Long playRoomId) {
        playRoomService.deletePlayRoom(playRoomId);
        return ok().body(new DeletePlayRoomResponse("합주실 삭제 완료"));
    }

    @Operation(summary = "합주실 수정 API", description = "연주 중인 세션 보다 적은 인원으로 수정하는 경우 에러 발생")
    @PutMapping("/{playRoomId}/edit")
    public ResponseEntity<EditPlayRoomRequest> editPlayRoom(@RequestBody @Valid EditPlayRoomRequest request, @PathVariable("playRoomId") Long playRoomId) {
        playRoomService.editPlayRoom(playRoomId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "합주실 상세보기 페이지")
    @PutMapping("/{playRoomId}/detailInfo")
    public ResponseEntity<PlayRoomInfoResponse> getPlayRoomInfo(@PathVariable("playRoomId") Long playRoomId) {
        PlayRoomInfoResponse response = playRoomService.getPlayRoomInfo(playRoomId);
        return ResponseEntity.ok().body(response);
    }


    @Operation(summary = "대표 이미지 업로드", description = "합주실 상세 페이지에서 파일을 업로드하고 저장하기 전에 요청보내야함.")
    @PostMapping(value = "/{playRoomId}/upload-img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadPlayRoomImgResponse> uploadProfileImage(
            @PathVariable Long playRoomId,
            @RequestPart MultipartFile file) throws Exception {
        var response = playRoomService.uploadImage(playRoomId, file);
        return ResponseEntity.ok(response);
    }
}
