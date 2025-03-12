package com.example.jampot.domain.user.service;

import com.example.jampot.domain.auth.dto.response.CustomOAuth2User;
import com.example.jampot.domain.auth.dto.response.UserLoginResponse;
import com.example.jampot.domain.common.repository.GenreRepository;
import com.example.jampot.domain.common.domain.Genre;
import com.example.jampot.domain.common.domain.Session;
import com.example.jampot.domain.common.repository.SessionRepository;
import com.example.jampot.domain.common.vo.Role;
import com.example.jampot.domain.user.dto.request.MypageEditRequest;
import com.example.jampot.domain.user.dto.request.UserJoinRequest;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.dto.response.MypageResponse;
import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.domain.user.vo.Provider;
import com.example.jampot.global.util.AuthUtil;
import com.example.jampot.global.util.JWTUtil;
import com.nimbusds.jwt.JWT;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.lang.String.valueOf;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final SessionRepository sessionRepository;
    private final JWTUtil jwtUtil;
    private final AuthUtil authUtil;

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public List<String> joinUser( UserJoinRequest userJoinRequest){
        logger.info("joinUser");
        String providerAndId = SecurityContextHolder.getContext().getAuthentication().getName();
        String[] parts = providerAndId.split("_");
        Provider provider = Provider.fromString(valueOf(parts[0]));
        String providerId = parts[1];

        if(userRepository.findByProviderAndProviderId(provider, providerId) != null){//기존 회원인 경우
            throw new IllegalStateException("이미 가입된 회원입니다");
        }

        List<Session> selectedSessions = sessionRepository.findByNameIn(userJoinRequest.sessionList());
        List<Genre> selectedGenres = genreRepository.findByNameIn(userJoinRequest.genreList());

        //TODO(user builder 서비스에 작성)
        //신규 가입 회원 저장
        User newUser = User.createUser(provider, providerId, Role.USER, userJoinRequest.nickname(),
                                        selectedSessions, selectedGenres, userJoinRequest.isPublic());

        userRepository.save(newUser);

        List<String> jwts =  new ArrayList<>();

        //JWT 발급 (회원가입 후 바로 로그인 상태 유지)
        String accessToken = jwtUtil.createJwt(newUser.getProvider()+"_"+newUser.getProviderId(), "USER");
        String refreshToken = jwtUtil.createJwt(newUser.getProvider()+"_"+newUser.getProviderId(), "USER");
        jwts.add(accessToken); jwts.add(refreshToken);

        //contextholder에 저장
        // 사용자 정보를 담은 DTO 생성
        UserLoginResponse userLoginResponse = new UserLoginResponse();
        userLoginResponse.setProviderAndId(providerAndId);
        userLoginResponse.setRole("USER");

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userLoginResponse);
        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        //세션에 사용자 정보 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        return jwts;
    }

    public MypageResponse getUserMypageInfo() {
        User user = authUtil.getLoggedInUser();

        List<String> sessionList = user.getUserSessionList().stream()
                .map(userSession -> userSession.getSession().getName())
                .toList();
        List<String> genreList = user.getUserGenreList().stream()
                .map(userGenre -> userGenre.getGenre().getName())
                .toList();

        return new MypageResponse(user.getNickName(), user.getSelfIntroduction(), user.getProfileImgUrl(), user.getAudioFileUrl(), user.getIsPublic(), sessionList, genreList);
    }


    /*

    //TODO(마이페이지 수정)
    public void editMypageInfo(MypageEditRequest mypageEditRequest){
        // 새로운 장르 추가
        List<Genre> selectedGenres = genreRepository.findByNameIn(mypageEditRequest.genreList());

    }
    //TODO(파일 s3에 업로드)
    public Object uploadProfileImage(MultipartFile file, String providerAndId) {
        try{
            String fileName = generateProfileImageFileName(providerAndId);
            String profileImageUrl = file.getOriginalFilename();
        }
    }

     */


    private String generateProfileImageFileName(String providerAndId) {
        try{
            var md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(providerAndId.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().encodeToString(hash);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }

    }

}