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
import com.example.jampot.domain.user.dto.response.UserProfileAudioUploadResponse;
import com.example.jampot.domain.user.dto.response.UserProfileImgUploadResponse;
import com.example.jampot.domain.user.repository.UserRepository;
import com.example.jampot.domain.user.vo.Provider;
import com.example.jampot.global.properties.CookieProperties;
import com.example.jampot.global.util.AuthUtil;
import com.example.jampot.global.util.JWTUtil;
import com.example.jampot.global.util.ProfileAudioUtil;
import com.example.jampot.global.util.ProfileImageUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.Optional;

import static java.lang.String.valueOf;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final SessionRepository sessionRepository;
    private final JWTUtil jwtUtil;
    private final AuthUtil authUtil;
    private final ProfileImageUtil profileImageUtil;
    private final ProfileAudioUtil profileAudioUtil;

    private static Logger logger = LoggerFactory.getLogger(UserService.class);
    private final CookieProperties cookieProperties;

    @Transactional
    public void joinUser( UserJoinRequest userJoinRequest, HttpServletResponse httpServletResponse) {
        String providerAndId = SecurityContextHolder.getContext().getAuthentication().getName();
        String[] parts = providerAndId.split("_");
        Provider provider = Provider.fromString(valueOf(parts[0]));
        String providerId = parts[1];

        if(!userRepository.findByProviderAndProviderId(provider, providerId).isEmpty()){//기존 회원인 경우
            throw new IllegalStateException("이미 가입된 회원입니다");
        }

        if(!userJoinRequest.nickname().isEmpty() && userRepository.findByNickName(userJoinRequest.nickname()).isPresent()){
            throw new IllegalStateException("이미 사용중인 닉네임입니다.");
        }

        List<Session> selectedSessions = sessionRepository.findByNameIn(userJoinRequest.sessionList());
        List<Genre> selectedGenres = genreRepository.findByNameIn(userJoinRequest.genreList());

        //신규 가입 회원 저장
        User newUser = User.createUser(provider, providerId, Role.USER, userJoinRequest.nickname(),
                                        selectedSessions, selectedGenres, userJoinRequest.isPublic());

        userRepository.save(newUser);


        jwtUpdate(httpServletResponse, newUser, providerAndId);

    }

    @Transactional
    public void logoutUser(HttpServletResponse response){
        cookieInvalid(response);
    }

    @Transactional
    public void deleteUser(HttpServletResponse response){
        User user = authUtil.getLoggedInUser();
        userRepository.delete(user);
        cookieInvalid(response);
    }



    @Transactional
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



    @Transactional
    public void editMypageInfo(MypageEditRequest mypageEditRequest){

        User loggedInUser = authUtil.getLoggedInUser();

        if(mypageEditRequest.nickName() != null){
            Optional<User> extistingUser = userRepository.findByNickName(mypageEditRequest.nickName());

            if(extistingUser.isPresent() && !extistingUser.get().equals(loggedInUser)){
                throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
            }
        }

        // 새로운 장르 추가
        List<Genre> selectedGenres = genreRepository.findByNameIn(mypageEditRequest.genreList());
        List<Session> selectedSessions = sessionRepository.findByNameIn(mypageEditRequest.sessionList());

        //TODO(캘린더 연동)

        loggedInUser.updateUser(
                mypageEditRequest.nickName(),
                mypageEditRequest.selfIntroduction(),
                selectedSessions,
                selectedGenres,
                mypageEditRequest.profileImageUrl(),
                mypageEditRequest.profileAudioUrl(),
                mypageEditRequest.calenderServiceAgreement(),  // 캘린더 동의는 따로 받지 않으므로 null 유지
                mypageEditRequest.isPublic()
        );


    }
    @Transactional
    public UserProfileImgUploadResponse uploadProfileImage(MultipartFile file) throws Exception {
        String providerAndId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            String fileName = generateProfileFileName(providerAndId);
            String profileImageUrl = profileImageUtil.uploadImageFile(file, fileName);
            return new UserProfileImgUploadResponse(profileImageUrl);
        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    @Transactional
    public UserProfileAudioUploadResponse uploadProfileAudio(MultipartFile file) throws Exception {
        String providerAndId = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            String fileName = generateProfileFileName(providerAndId);
            String profileImageUrl = profileAudioUtil.uploadAudioFile(file, fileName);
            return new UserProfileAudioUploadResponse(profileImageUrl);
        }catch (Exception e){
            throw  new RuntimeException();
        }
    }


    private String generateProfileFileName(String providerAndId) {
        try{
            var md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(providerAndId.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().encodeToString(hash);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }

    }
    //소셜 로그인(jwt 발급)
    private void jwtUpdate(HttpServletResponse response,User newUser, String providerAndId){

        //JWT 발급 (회원가입 후 바로 로그인 상태 유지)
        String accessToken = jwtUtil.createJwt(newUser.getProvider()+"_"+newUser.getProviderId(), "USER");
        String refreshToken = jwtUtil.createJwt(newUser.getProvider()+"_"+newUser.getProviderId(), "USER");

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


        //쿠키 생성
        Cookie accessCookie = new Cookie("AccessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 60 * 5); //5시간

        Cookie refreshCookie = new Cookie("RefreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24); //하루

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    //쿠키 삭제(로그아웃, 회원 탈퇴)
    private void cookieInvalid(HttpServletResponse response){
        Cookie accessTokenCookie = new Cookie("AccessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setSecure(cookieProperties.getCookieSecure());

        Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setSecure(cookieProperties.getCookieSecure());

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

}