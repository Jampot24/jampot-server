package com.example.jampot.domain.user.repository;

import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.vo.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByProviderAndProviderId(Provider provider, String providerId);
}
