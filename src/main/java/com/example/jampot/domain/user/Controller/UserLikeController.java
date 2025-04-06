package com.example.jampot.domain.user.Controller;

import com.example.jampot.domain.user.dto.response.LikeUserResponse;
import com.example.jampot.domain.user.dto.response.UnlikeUserResponse;
import com.example.jampot.domain.user.service.UserLikeService;
import com.example.jampot.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
@Tag(name = "UserLike", description = "사용자 찜 관련 API")
public class UserLikeController {

    private final UserLikeService userLikeService;

    @Operation(summary = "찜하기 기능")
    @PutMapping("/{toUserId}")
    public ResponseEntity<LikeUserResponse> likeUser(@PathVariable Long toUserId) {
        userLikeService.likeUser(toUserId);
        return ResponseEntity.ok().body(new LikeUserResponse("찜 추가 완료"));
    }

    @Operation(summary = "찜 취소 기능")
    @DeleteMapping("/{toUserId}")
    public ResponseEntity<UnlikeUserResponse> unlikeUser(@PathVariable Long toUserId) {
        userLikeService.unlikeUser(toUserId);
        return ResponseEntity.ok().body(new UnlikeUserResponse("찜 삭제 완료"));
    }
}
