package com.example.jampot.domain.playRoom.repository;

import com.example.jampot.domain.playRoom.domain.PlayRoom;
import com.example.jampot.domain.playRoom.domain.PlayRoomLike;
import com.example.jampot.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayRoomLikeRepository extends JpaRepository<PlayRoomLike, Long> {

    boolean existsByPlayRoomAndUser(PlayRoom playRoom, User user);

    Optional<PlayRoomLike> findByPlayRoomAndUser(PlayRoom playRoom, User user);

    @Query("SELECT pl.playRoom FROM PlayRoomLike pl WHERE pl.user =:user")
    List<PlayRoom> findPlayRoomByUser(User user);
}
