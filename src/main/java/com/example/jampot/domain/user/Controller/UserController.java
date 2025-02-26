package com.example.jampot.domain.user.Controller;

import com.example.jampot.domain.auth.dto.request.UserJoinRequest;
import com.example.jampot.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    public final UserService userService;
    @PostMapping("/join")
    public ResponseEntity<?> completeJoin (@RequestBody UserJoinRequest userJoinRequest, HttpServletResponse response){
        try{
            String token = userService.joinUser(userJoinRequest);

            Cookie jwtCookie = new Cookie("Authorization", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(jwtCookie);

            return ResponseEntity.ok().body("회원가입 완료 및 JWT 발급");
        }catch(IllegalStateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
