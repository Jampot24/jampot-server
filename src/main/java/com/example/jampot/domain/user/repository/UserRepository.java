package com.example.jampot.domain.user.repository;

import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.vo.Provider;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByNickName(@NotEmpty String nickname);
}
