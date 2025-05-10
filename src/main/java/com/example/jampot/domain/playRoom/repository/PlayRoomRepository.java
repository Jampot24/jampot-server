package com.example.jampot.domain.playRoom.repository;

import com.example.jampot.domain.playRoom.domain.PlayRoom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayRoomRepository extends JpaRepository<PlayRoom, Long> {

    Optional<PlayRoom> findByName(@NotNull(message = "name cannot be null") @NotBlank(message = "name cannot be empty or blank") @Size(min = 1, message = "name must have at least one character") String name);

    List<PlayRoom> findByIsPlayerLocked(boolean isPlayerLocked);
}
