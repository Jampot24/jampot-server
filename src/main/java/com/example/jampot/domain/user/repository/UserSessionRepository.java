package com.example.jampot.domain.user.repository;

import com.example.jampot.domain.user.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession,Long > {

}
