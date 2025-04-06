package com.example.jampot.domain.user.repository;

import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.domain.UserLike;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLikeRepository extends CrudRepository<UserLike, Long> {
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    Optional<UserLike> findByFromUserAndToUser(User fromUser, User toUser);

    @Query("SELECT ul.toUser FROM UserLike ul WHERE ul.fromUser = :fromUser")
    List<User> findToUsersByFromUser(@Param("fromUser") User fromUser);
}
