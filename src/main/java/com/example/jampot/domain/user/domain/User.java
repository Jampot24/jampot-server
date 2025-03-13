package com.example.jampot.domain.user.domain;

import com.example.jampot.domain.common.domain.BaseEntity;
import com.example.jampot.domain.common.domain.Genre;
import com.example.jampot.domain.common.domain.Session;
import com.example.jampot.domain.common.vo.Role;
import com.example.jampot.domain.user.vo.Provider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;
    @Column
    @Enumerated(EnumType.STRING)
    private Provider provider;
    @Column
    private String providerId;
    @Column
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column
    private String nickName;//닉네임
    @Column
    private String selfIntroduction;
    @Column
    private String profileImgUrl;
    @Column
    private String audioFileUrl;
    @Column
    private Boolean calenderServiceAgreement;
    @Column
    private Boolean isPublic;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSession> userSessionList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGenre> userGenreList = new ArrayList<>();


    // 🔹 객체 생성 (빌더 사용)
    @Builder(access = AccessLevel.PRIVATE)
    private User(Provider provider, String providerId,
                 Role role, String nickName, String selfIntroduction,
                 String profileImgUrl, String audioFileUrl, Boolean calenderServiceAgreement, Boolean isPublic) {
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
        this.nickName = nickName;
        this.selfIntroduction = selfIntroduction;
        this.profileImgUrl = profileImgUrl;
        this.audioFileUrl = audioFileUrl;
        this.calenderServiceAgreement = calenderServiceAgreement;
        this.isPublic = isPublic;
    }

    // 🔹 User 객체 생성 메서드 (정적 메서드) - user 생성 request에서 받아오는 필드만 파라미터에 작성
    public static User createUser(Provider provider, String providerId, Role role,
                                  String nickName, List<Session> selectedSessions, List<Genre> selectedGenres, Boolean isPublic) {
        User newUser =  User.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .role(role)
                    .nickName(nickName)
                    .selfIntroduction("")
                    .profileImgUrl("")
                    .audioFileUrl("")
                    .calenderServiceAgreement(false)
                    .isPublic(isPublic)
                    .build();

        newUser.genreListToUserGenreList(selectedGenres);
        newUser.sessionListToUserSessionList(selectedSessions);

        return newUser;

    }

    // 🔹 User 객체 업데이트 (변경된 필드만 반영)
    public void updateUser(String nickName, String selfIntroduction,
                           List<Session> selectedSessions, List<Genre> selectedGenres,
                           String profileImgUrl, String audioFileUrl,
                           Boolean calenderServiceAgreement, Boolean isPublic) {

        if (nickName != null) this.nickName = nickName;
        if (selfIntroduction != null) this.selfIntroduction = selfIntroduction;
        if (profileImgUrl != null) this.profileImgUrl = profileImgUrl;
        if (audioFileUrl != null) this.audioFileUrl = audioFileUrl;
        if (calenderServiceAgreement != null) this.calenderServiceAgreement = calenderServiceAgreement;
        if (isPublic != null) this.isPublic = isPublic;

        // 기존 세션 및 장르 업데이트 (전체 삭제 후 재추가)
        updateSessions(selectedSessions);
        updateGenres(selectedGenres);
    }

    public void updateGenres(List<Genre> selectedGenres) {
        //기존 장르 삭제 (orphanRemoval=true 덕분에 자동으로 DB에서 삭제됨)
        userGenreList.clear();

        // 새로 받은 장르 목록으로 UserGenre 객체 생성
        for (Genre genre : selectedGenres) {
            userGenreList.add(UserGenre.create(this, genre));
        }
    }

    public void updateSessions(List<Session> selectedSessions) {
        //기존 장르 삭제 (orphanRemoval=true 덕분에 자동으로 DB에서 삭제됨)
        userSessionList.clear();

        // 새로 받은 장르 목록으로 UserGenre 객체 생성
        for (Session session : selectedSessions) {
            userSessionList.add(UserSession.create(this, session));
        }
    }

    public List<UserGenre> genreListToUserGenreList(List<Genre> selectedGenres) {
        // 새로 받은 장르 목록으로 UserGenre 객체 생성
        for (Genre genre : selectedGenres) {
            userGenreList.add(UserGenre.create(this, genre));
        }
        return userGenreList;
    }

    public List<UserSession> sessionListToUserSessionList(List<Session> selectedSessions) {
        for (Session session : selectedSessions) {
            userSessionList.add(UserSession.create(this, session));
        }
        return userSessionList;
    }
}
