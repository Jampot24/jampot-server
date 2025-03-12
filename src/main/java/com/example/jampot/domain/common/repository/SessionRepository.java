package com.example.jampot.domain.common.repository;

import com.example.jampot.domain.common.domain.Genre;
import com.example.jampot.domain.common.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByNameIn(List<String> selectedSessionNames);
}
