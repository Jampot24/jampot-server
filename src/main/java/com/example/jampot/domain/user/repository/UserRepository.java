package com.example.jampot.domain.user.repository;

import com.example.jampot.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByProviderAndProviderId(String provider, String providerId);
}
