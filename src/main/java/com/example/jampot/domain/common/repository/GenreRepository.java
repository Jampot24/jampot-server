package com.example.jampot.domain.common.repository;

import com.example.jampot.domain.common.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findByNameIn(List<String> selectedGenreNames);
}
