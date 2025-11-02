package com.krypt.backend.repository;

import com.krypt.backend.model.Note;
import com.krypt.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserOrderByTitleAsc(User user);
}