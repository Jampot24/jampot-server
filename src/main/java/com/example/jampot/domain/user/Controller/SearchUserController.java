package com.example.jampot.domain.user.Controller;

import com.example.jampot.domain.user.dto.response.SearchUserResponse;
import com.example.jampot.domain.user.service.SearchUserService;
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
@RequestMapping("/search/users")
@Tag(name = "UserSearch", description = "사용자 검색 API")
public class SearchUserController {

    private final SearchUserService userSearchService;

    @Operation(summary = "닉네임, 세션, 장르 검색", description = "검색 조건(닉네임, 세션, 장르), 파라미터가 없는 경우 모든 사용자 반환")
    @GetMapping("/condition")
    public ResponseEntity<SearchUserResponse> searchUsers(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) List<String> sessionList,
            @RequestParam(required = false) List<String> genreList

    ) {
        SearchUserResponse userSearchResponse = userSearchService.searchUsersByCondition(nickname, sessionList, genreList);
        return ResponseEntity.ok().body(userSearchResponse);
    }

    @Operation(summary = "유저 찜 리스트 보기")
    @GetMapping("/liked")
    public ResponseEntity<SearchUserResponse> searchUsersByLiked() {
        SearchUserResponse userSearchResponse = userSearchService.searchUsersByLiked();
        return ResponseEntity.ok().body(userSearchResponse);
    }
}
