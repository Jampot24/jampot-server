package com.example.jampot.domain.playRoom.controller;

import com.example.jampot.domain.playRoom.dto.request.CreatePlayRoomRequest;
import com.example.jampot.domain.playRoom.dto.request.EditPlayRoomRequest;
import com.example.jampot.domain.playRoom.dto.request.EnterPlayRoomAsAudienceRequest;
import com.example.jampot.domain.playRoom.dto.request.EnterPlayRoomAsPlayerRequest;
import com.example.jampot.domain.playRoom.dto.response.*;
import com.example.jampot.domain.playRoom.service.PlayRoomService;
import com.example.jampot.domain.playRoom.service.RedisSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    @GetMapping("/{playRoomId}/detailInfo")
    public ResponseEntity<PlayRoomInfoResponse> getPlayRoomInfo(@PathVariable("playRoomId") Long playRoomId) {
        PlayRoomInfoResponse response = playRoomService.getPlayRoomInfo(playRoomId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "합주실 입장 요청 전 잔여 세션 반환")
    @GetMapping("/{playRoomId}/available-sessions")
    public ResponseEntity<AvailableSessionListResponse> getAvailableSessions(@PathVariable Long playRoomId){
        AvailableSessionListResponse sessions = playRoomService.getAvailableSessions(playRoomId);
        return ResponseEntity.ok().body(sessions);
    }
/*
    @Operation(summary = "합주실 입장 후 연주자로 참여한 사용자 정보 반환")
    @GetMapping("/{playRoomId}/participants")
    public ResponseEntity<PlayRoomStatusResponse> getPlayRoomStatus(
            @PathVariable("playRoomId") Long playRoomId
    ){
        PlayRoomStatusResponse response = playRoomService.getPlayRoomStatus(playRoomId);
        return ResponseEntity.ok().body(response);
    }
*/
    @Operation(summary = "연주자 합주실 입장 요청", description = "연주자 비밀번호, 잔여 세션 확인 후 합주실 입장")
    @PostMapping("/{playRoomId}/enter/player")
    public ResponseEntity<EnterPlayRoomResponse> enterPlayRoom(
            @PathVariable Long playRoomId,
            @Valid @RequestBody EnterPlayRoomAsPlayerRequest request) {
        EnterPlayRoomResponse response = playRoomService.enterAsPlayer(playRoomId, request);

        if(response.success()){
            PlayRoomStatusResponse status = playRoomService.getPlayRoomStatusAsPlayer(playRoomId);
            response = new EnterPlayRoomResponse(true, null, status);
        }

        return ResponseEntity.ok().body(response);
    }


    @Operation(summary = "관객 합주실 입장 요청", description = "관객 비밀번호 확인 후 합주실 입장")
    @PostMapping("/{playRoomId}/enter/audience")
    ResponseEntity<EnterPlayRoomResponse> enterPlayRoom(
            @PathVariable Long playRoomId,
            @RequestBody EnterPlayRoomAsAudienceRequest request) {
        EnterPlayRoomResponse response = playRoomService.enterAsAudience(playRoomId, request);

        if(response.success()){
            PlayRoomStatusResponse status = playRoomService.getPlayRoomStatusAsAudience(playRoomId);
            response = new EnterPlayRoomResponse(true, null, status);
        }
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "연주자 합주실 퇴장")
    @PutMapping("/{playRoomId}/exist/player")
    ResponseEntity<Void> existPlayRoomAsPlayer(
            @PathVariable Long playRoomId
    ){
        playRoomService.existAsPlayer(playRoomId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "관중 합주실 퇴장")
    @PutMapping("/{playRoomId}/exist/audience")
    ResponseEntity<Void> existPlayRoomAsAudience(
            @PathVariable Long playRoomId
    ){
        playRoomService.existAsAudience(playRoomId);
        return ResponseEntity.ok().build();
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
