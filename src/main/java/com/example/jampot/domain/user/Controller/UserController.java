package com.example.jampot.domain.user.Controller;

import com.example.jampot.domain.user.dto.request.MypageEditRequest;
import com.example.jampot.domain.user.dto.request.UserJoinRequest;
import com.example.jampot.domain.user.dto.response.*;
import com.example.jampot.domain.user.service.UserService;
import com.example.jampot.global.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {
    private final JWTUtil jwtUtil;
    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping("/join")
    public ResponseEntity<JoinUserResponse> completeJoin (@RequestBody @Valid UserJoinRequest userJoinRequest, HttpServletResponse response){
        try{
            userService.joinUser(userJoinRequest, response);

            return ResponseEntity.ok().body(new JoinUserResponse("회원가입 완료 및 JWT 발급"));
        }catch(IllegalStateException e){
            return ResponseEntity.badRequest().body(new JoinUserResponse(e.getMessage()));
        }
    }

    @Operation(summary = "로그아웃", description = "토큰 무효화")
    @PostMapping("/logout")
    public ResponseEntity<LogoutUserResponse> userLogout(HttpServletResponse response){
        userService.logoutUser(response);
        return ResponseEntity.ok().body(new LogoutUserResponse("로그아웃 완료 및 JWT 무효화"));
    }

    @Operation(summary = "회원 탈퇴", description = "DB에서 회원 정보 삭제, 토큰 무효화")
    @DeleteMapping("/delete")
    public ResponseEntity<DeleteUserResponse> userDelete (HttpServletResponse response){
        userService.deleteUser(response);
        return ResponseEntity.ok().body(new DeleteUserResponse("회원 탈퇴 완료 및 JWT 무효화"));
    }


    @Operation(summary = "마이페이지 보기")
    @GetMapping("/mypage")
    public ResponseEntity<MypageResponse> getMyInfo(){
        MypageResponse mypageResponse = userService.getUserMypageInfo();
        return ResponseEntity.ok().body(mypageResponse);
    }

    @Operation(summary = "마이페이지 수정")
    @PutMapping("/mypage/edit")
    public ResponseEntity<Void> editMypageInfo( @Valid @RequestBody MypageEditRequest mypageEditRequest){
        userService.editMypageInfo(mypageEditRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary="타인의 프로필 보기")
    @GetMapping("/{userId}/detail-info")
    public ResponseEntity<MypageTargetResponse> getTargetInfo(@PathVariable("userId") Long userId){
        MypageTargetResponse mypageTargetResponse = userService.getTargetMypageInfo(userId);
        return ResponseEntity.ok().body(mypageTargetResponse);
    }

    @Operation(summary = "프로필 이미지 업로드", description = "마이페이지에서 파일을 업로드하고 저장하기 전에 요청보내야함.")
    @PostMapping(value = "/upload-profile-img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadUserProfileImgResponse> uploadProfileImage(
            @RequestPart MultipartFile file) throws Exception {
        var response = userService.uploadProfileImage(file);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 음성 업로드", description = "마이페이지에서 파일을 업로드하고 저장하기 전에 요청보내야함.")
    @PostMapping(value = "/upload-profile-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadUserProfileAudioResponse> uploadProfileAudio(
            @RequestPart MultipartFile file) throws Exception{
        var response = userService.uploadProfileAudio(file);
        return ResponseEntity.ok(response);
    }
}
