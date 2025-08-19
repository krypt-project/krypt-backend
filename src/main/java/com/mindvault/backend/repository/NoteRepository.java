package com.mindvault.backend.repository;

import com.mindvault.backend.model.Note;
import com.mindvault.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {
    List<Note> findByUser(User user);
}
