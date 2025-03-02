package com.example.jampot.domain.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String provider;
    @Column
    private String providerId;
    @Column
    private String role;
    @Column
    private String nickName;//닉네임
    @Column
    private String selfIntroduction;
    @Column
    private String profileImgUrl;
    @Column
    private String audioFileUrl;
    @Column
    private Boolean calender_service_agreement;
    @Column
    private Boolean isPublic;

    //닉네임, 세션, 장르, 공개여부 설정
}
