package com.example.jampot.domain.user.repository;

import com.example.jampot.domain.user.domain.User;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> searchByAllConditions(String nickName, List<String> sessionNames, List<String> genreNames);
}
