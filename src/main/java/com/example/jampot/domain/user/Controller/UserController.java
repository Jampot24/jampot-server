package com.example.jampot.domain.user.Controller;

import com.example.jampot.domain.user.dto.request.MypageEditRequest;
import com.example.jampot.domain.user.dto.request.UserJoinRequest;
import com.example.jampot.domain.user.dto.response.MypageResponse;
import com.example.jampot.domain.user.dto.response.UserProfileUploadResponse;
import com.example.jampot.domain.user.service.UserService;
import com.example.jampot.global.util.JWTUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {
    private final JWTUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> completeJoin (@RequestBody UserJoinRequest userJoinRequest, HttpServletResponse response){
        try{
            List<String> jwts = userService.joinUser(userJoinRequest);

            //쿠키 생성
            Cookie accessCookie = new Cookie("AccessToken", jwts.get(0));
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(60 * 60 * 5);

            Cookie refreshCookie = new Cookie("RefreshToken", jwts.get(1));
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(60 * 60 * 24);

            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);

            return ResponseEntity.ok().body("회원가입 완료 및 JWT 발급");
        }catch(IllegalStateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/mypage")
    public ResponseEntity<MypageResponse> getMyInfo(HttpServletRequest request){

        MypageResponse mypageResponse = userService.getUserMypageInfo();
        return ResponseEntity.ok().body(mypageResponse);
    }

    private String getJwtFromCookies(HttpServletRequest request){
        if (request.getCookies() != null){
            for (Cookie cookie : request.getCookies()){
                if(cookie.getName().equals("AccessToken")){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /*
    //TODO(마이페이지 수정)
    public ResponseEntity<Void> editMypageInfo(HttpServletRequest request, MypageEditRequest mypageEditRequest){
        String token = getJwtFromCookies(request);
        if(token == null){}
    }


    //TODO(이미지 파일 업로드)
    public ResponseEntity<UserProfileUploadResponse> uploadProfileImage(
            @RequestPart MultipartFile file,
            HttpServletRequest request){
        String token = getJwtFromCookies(request);
        if(token == null){
            return ResponseEntity.status(401).build();
        }
        String providerAndId = jwtUtil.getProviderAndId(token);

        var response = userService.uploadProfileImage(file,providerAndId);
        return ResponseEntity.ok().body(response);
    }
    //TODO 음성 파일 s3 업로드

     */

}
